package com.healthybites.community.app;

import com.healthybites.community.domain.CommunityGroup;
import com.healthybites.community.domain.CommunityGroupRepository;
import com.healthybites.community.domain.GroupMembership;
import com.healthybites.community.domain.GroupMembershipRepository;
import com.healthybites.community.events.MembershipRequested;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.shared.web.ApiException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grupos y membresías. Los grupos se ven en el directorio aunque sean privados (para pedir entrar), pero sus
 * publicaciones solo las leen los miembros activos.
 */
@Service
public class GroupService {

    private final CommunityGroupRepository groups;
    private final GroupMembershipRepository memberships;
    private final UserAccounts accounts;
    private final ApplicationEventPublisher events;

    public GroupService(
            CommunityGroupRepository groups,
            GroupMembershipRepository memberships,
            UserAccounts accounts,
            ApplicationEventPublisher events) {
        this.groups = groups;
        this.memberships = memberships;
        this.accounts = accounts;
        this.events = events;
    }

    /** Un grupo con su número de miembros activos y la membresía del lector (si tiene). */
    public record GroupCard(
            CommunityGroup group, long members, @Nullable GroupMembership membership) {}

    public record GroupData(String name, @Nullable String description, CommunityGroup.Visibility visibility) {}

    public record Member(
            GroupMembership membership, @Nullable UserAccount account) {}

    @Transactional(readOnly = true)
    public Page<GroupCard> directory(UUID userId, @Nullable String query, Pageable pageable) {
        Specification<CommunityGroup> spec = Specification.unrestricted();
        if (query != null && !query.isBlank()) {
            String like = "%"
                    + query.trim().toLowerCase(Locale.ROOT).replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("name")), like, '\\'));
        }
        Page<CommunityGroup> page = groups.findAll(spec, pageable);
        List<UUID> ids =
                page.getContent().stream().map(CommunityGroup::requireId).toList();
        Map<UUID, Long> counts = counts(ids);
        Map<UUID, GroupMembership> mine = ids.isEmpty()
                ? Map.of()
                : memberships.findByUserIdAndGroupIdIn(userId, ids).stream()
                        .collect(Collectors.toMap(GroupMembership::getGroupId, Function.identity()));
        return page.map(
                group -> new GroupCard(group, counts.getOrDefault(group.requireId(), 0L), mine.get(group.requireId())));
    }

    @Transactional(readOnly = true)
    public GroupCard get(UUID groupId, UUID userId) {
        return card(find(groupId), userId);
    }

    @Transactional
    public GroupCard create(UUID ownerId, GroupData data) {
        CommunityGroup group =
                groups.save(CommunityGroup.create(ownerId, data.name(), data.description(), data.visibility()));
        memberships.save(GroupMembership.of(
                group.requireId(), ownerId, GroupMembership.Role.OWNER, GroupMembership.Status.ACTIVE));
        return card(group, ownerId);
    }

    @Transactional
    public GroupCard update(UUID groupId, UUID userId, GroupData data) {
        CommunityGroup group = owned(groupId, userId);
        group.update(data.name(), data.description(), data.visibility());
        return card(group, userId);
    }

    @Transactional
    public void delete(UUID groupId, UUID userId, boolean admin) {
        CommunityGroup group = find(groupId);
        if (!admin && !group.isOwnedBy(userId)) {
            throw notOwner();
        }
        groups.delete(group);
    }

    /** Público: entra al momento. Privado: queda pendiente hasta que el creador apruebe. */
    @Transactional
    public GroupCard join(UUID groupId, UUID userId) {
        CommunityGroup group = find(groupId);
        if (memberships.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw ApiException.conflict("already-member", "Ya perteneces a este grupo o tienes la solicitud pendiente");
        }
        GroupMembership.Status status =
                group.isPublic() ? GroupMembership.Status.ACTIVE : GroupMembership.Status.PENDING;
        memberships.save(GroupMembership.of(groupId, userId, GroupMembership.Role.MEMBER, status));
        if (!group.isPublic()) {
            events.publishEvent(new MembershipRequested(groupId, userId, group.getOwnerId()));
        }
        return card(group, userId);
    }

    @Transactional
    public void leave(UUID groupId, UUID userId) {
        CommunityGroup group = find(groupId);
        if (group.isOwnedBy(userId)) {
            throw ApiException.conflict("owner-cannot-leave", "Eres el creador: borra el grupo si quieres cerrarlo");
        }
        memberships.findByGroupIdAndUserId(groupId, userId).ifPresent(memberships::delete);
    }

    /** Miembros activos (para los miembros) y también las solicitudes pendientes (para el creador). */
    @Transactional(readOnly = true)
    public List<Member> members(UUID groupId, UUID userId) {
        CommunityGroup group = find(groupId);
        requireActiveMember(group, userId);
        boolean owner = group.isOwnedBy(userId);
        List<GroupMembership> list = memberships.findByGroupIdOrderByCreatedAtAsc(groupId).stream()
                .filter(m -> owner || m.isActive())
                .toList();
        Map<UUID, UserAccount> people = accounts.findAllById(
                list.stream().map(GroupMembership::getUserId).toList());
        return list.stream().map(m -> new Member(m, people.get(m.getUserId()))).toList();
    }

    @Transactional
    public void approve(UUID groupId, UUID memberId, UUID ownerId) {
        owned(groupId, ownerId);
        memberships
                .findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(
                        () -> ApiException.notFound("membership-not-found", "No hay ninguna solicitud de esa persona"))
                .approve();
    }

    /** Rechaza una solicitud o expulsa a un miembro (nunca al creador). */
    @Transactional
    public void remove(UUID groupId, UUID memberId, UUID ownerId) {
        owned(groupId, ownerId);
        if (memberId.equals(ownerId)) {
            throw ApiException.conflict("owner-cannot-leave", "No puedes expulsarte de tu propio grupo");
        }
        memberships.findByGroupIdAndUserId(groupId, memberId).ifPresent(memberships::delete);
    }

    // ---------------------------------------------------------------- reglas de acceso (para PostService)

    CommunityGroup find(UUID groupId) {
        return groups.findById(groupId)
                .orElseThrow(() -> ApiException.notFound("group-not-found", "El grupo no existe"));
    }

    /** Leer publicaciones: grupos públicos, o miembro activo de uno privado. */
    void requireReader(CommunityGroup group, UUID userId) {
        if (!group.isPublic() && !isActiveMember(group, userId)) {
            throw ApiException.forbidden(
                    "private-group", "Este grupo es privado: pide entrar para ver sus publicaciones");
        }
    }

    /** Publicar y comentar: solo miembros activos. */
    void requireActiveMember(CommunityGroup group, UUID userId) {
        if (!isActiveMember(group, userId)) {
            throw ApiException.forbidden("not-a-member", "Únete al grupo para participar");
        }
    }

    private boolean isActiveMember(CommunityGroup group, UUID userId) {
        return memberships
                .findByGroupIdAndUserId(group.requireId(), userId)
                .map(GroupMembership::isActive)
                .orElse(false);
    }

    private CommunityGroup owned(UUID groupId, UUID userId) {
        CommunityGroup group = find(groupId);
        if (!group.isOwnedBy(userId)) {
            throw notOwner();
        }
        return group;
    }

    private GroupCard card(CommunityGroup group, UUID userId) {
        return new GroupCard(
                group,
                counts(List.of(group.requireId())).getOrDefault(group.requireId(), 0L),
                memberships.findByGroupIdAndUserId(group.requireId(), userId).orElse(null));
    }

    private Map<UUID, Long> counts(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return memberships.countActive(ids).stream()
                .collect(Collectors.toMap(
                        GroupMembershipRepository.GroupCount::getGroupId,
                        GroupMembershipRepository.GroupCount::getTotal));
    }

    private static ApiException notOwner() {
        return ApiException.forbidden("not-group-owner", "Solo el creador del grupo puede hacer esto");
    }
}
