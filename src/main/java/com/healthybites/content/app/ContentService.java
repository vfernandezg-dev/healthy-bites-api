package com.healthybites.content.app;

import com.healthybites.billing.Access;
import com.healthybites.billing.Entitlements;
import com.healthybites.content.ContentAccess;
import com.healthybites.content.domain.ContentEnums.ContentCategory;
import com.healthybites.content.domain.ContentEnums.ContentType;
import com.healthybites.content.domain.ContentEnums.UnlockSource;
import com.healthybites.content.domain.ContentItem;
import com.healthybites.content.domain.ContentItemRepository;
import com.healthybites.content.domain.ContentUnlock;
import com.healthybites.content.domain.ContentUnlockRepository;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.media.MediaFiles;
import com.healthybites.shared.domain.PublicationStatus;
import com.healthybites.shared.persistence.TextSearch;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de contenido, detalle con control de acceso, enlace al fichero y desbloqueos. Se abre lo BASIC, lo que
 * incluye el nivel del usuario y lo desbloqueado; el equipo (nutricionistas y ADMIN) lo abre todo.
 */
@Service
public class ContentService implements ContentAccess {

    /** Los enlaces firmados a ficheros duran poco: se piden al abrir el contenido. */
    public static final Duration FILE_LINK_TTL = Duration.ofMinutes(10);

    private final ContentItemRepository items;
    private final ContentUnlockRepository unlocks;
    private final Entitlements entitlements;
    private final UserAccounts accounts;
    private final MediaFiles media;
    private final Clock clock;

    public ContentService(
            ContentItemRepository items,
            ContentUnlockRepository unlocks,
            Entitlements entitlements,
            UserAccounts accounts,
            MediaFiles media,
            Clock clock) {
        this.items = items;
        this.unlocks = unlocks;
        this.entitlements = entitlements;
        this.accounts = accounts;
        this.media = media;
        this.clock = clock;
    }

    /** Un contenido con su autor, la ruta de la portada y si el lector puede abrirlo. */
    public record ContentCard(
            ContentItem item,
            @Nullable UserAccount author,
            @Nullable String coverPath,
            boolean locked) {}

    public record Filter(
            @Nullable ContentType type,
            @Nullable ContentCategory category,
            @Nullable String query) {}

    @Transactional(readOnly = true)
    public Page<ContentCard> catalog(UUID userId, boolean staff, Filter filter, Pageable pageable) {
        Specification<ContentItem> spec = (root, q, cb) -> cb.equal(root.get("status"), PublicationStatus.PUBLISHED);
        if (filter.type() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), filter.type()));
        }
        if (filter.category() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category"), filter.category()));
        }
        spec = spec.and(TextSearch.anyContains(filter.query(), "title", "summary"));
        Page<ContentItem> page = items.findAll(spec, pageable);
        Access reader = reader(userId, staff);
        Map<UUID, UserAccount> authors = accounts.findAllById(page.getContent().stream()
                .map(ContentItem::getAuthorId)
                .distinct()
                .toList());
        return page.map(item ->
                card(item, authors.get(item.getAuthorId()), !reader.canOpen(item.getRequiredTier(), item.requireId())));
    }

    /** Detalle por slug: 403 {@code subscription-required} (con {@code requiredTier}) si no se puede abrir. */
    @Transactional(readOnly = true)
    public ContentCard open(String slug, UUID userId, boolean staff) {
        ContentItem item = items.findBySlug(slug)
                .filter(candidate -> candidate.isVisibleTo(userId, staff))
                .orElseThrow(ContentService::notFound);
        requireOpen(item, userId, staff);
        return card(item, accounts.findById(item.getAuthorId()).orElse(null), false);
    }

    /** Ruta firmada y de vida corta al fichero del contenido, tras comprobar el acceso. */
    @Transactional(readOnly = true)
    public String fileLink(UUID itemId, UUID userId, boolean staff) {
        ContentItem item = items.findById(itemId)
                .filter(candidate -> candidate.isVisibleTo(userId, staff))
                .orElseThrow(ContentService::notFound);
        requireOpen(item, userId, staff);
        UUID file = item.getFileMediaId();
        if (file == null) {
            throw ApiException.notFound("no-file", "Este contenido no tiene fichero");
        }
        return media.signedPath(file, FILE_LINK_TTL);
    }

    @Transactional(readOnly = true)
    public List<ContentCard> unlockedBy(UUID userId) {
        List<ContentUnlock> mine = unlocks.findByUserIdOrderByUnlockedAtDesc(userId);
        Map<UUID, ContentItem> byId =
                items
                        .findAllById(mine.stream()
                                .map(ContentUnlock::getContentItemId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(ContentItem::requireId, Function.identity()));
        Map<UUID, UserAccount> authors = accounts.findAllById(
                byId.values().stream().map(ContentItem::getAuthorId).distinct().toList());
        return mine.stream()
                .map(unlock -> byId.get(unlock.getContentItemId()))
                .filter(item -> item != null && item.isPublished())
                .map(item -> card(item, authors.get(item.getAuthorId()), false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPublishedBy(UUID contentItemId, UUID authorId) {
        return items.isPublishedBy(contentItemId, authorId);
    }

    @Override
    @Transactional
    public void grant(UUID contentItemId, UUID userId) {
        if (!unlocks.existsByContentItemIdAndUserId(contentItemId, userId)) {
            unlocks.save(ContentUnlock.of(contentItemId, userId, UnlockSource.REWARD, clock.instant()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> titlesOf(Collection<UUID> contentItemIds) {
        return items.findAllById(contentItemIds).stream()
                .collect(Collectors.toMap(ContentItem::requireId, ContentItem::getTitle));
    }

    ContentCard card(ContentItem item, @Nullable UserAccount author, boolean locked) {
        UUID cover = item.getCoverMediaId();
        return new ContentCard(item, author, cover == null ? null : media.publicPath(cover), locked);
    }

    private void requireOpen(ContentItem item, UUID userId, boolean staff) {
        if (!reader(userId, staff).canOpen(item.getRequiredTier(), item.requireId())) {
            throw Access.subscriptionRequired(
                    item.getRequiredTier(),
                    "Este contenido requiere una suscripción " + item.getRequiredTier()
                            + " o desbloquearlo con una recompensa");
        }
    }

    private Access reader(UUID userId, boolean staff) {
        return staff ? Access.forStaff() : new Access(false, entitlements.tierOf(userId), unlocks.unlockedIds(userId));
    }

    static ApiException notFound() {
        return ApiException.notFound("content-not-found", "El contenido no existe");
    }
}
