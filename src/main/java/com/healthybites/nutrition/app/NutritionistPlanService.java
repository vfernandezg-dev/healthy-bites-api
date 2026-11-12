package com.healthybites.nutrition.app;

import com.healthybites.billing.Tier;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.nutrition.app.MealPlanService.PlanCard;
import com.healthybites.nutrition.domain.Meal;
import com.healthybites.nutrition.domain.MealPlan;
import com.healthybites.nutrition.domain.MealPlanEnrollment;
import com.healthybites.nutrition.domain.MealPlanEnrollmentRepository;
import com.healthybites.nutrition.domain.MealPlanEnrollmentRepository.ClientSince;
import com.healthybites.nutrition.domain.MealPlanRepository;
import com.healthybites.nutrition.domain.NutritionEnums.MealCategory;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.profiles.ClientProfiles;
import com.healthybites.profiles.ClientProfiles.ClientProfileView;
import com.healthybites.shared.domain.PublicationStatus;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Espacio del nutricionista: sus planes (borrador → publicado → archivado), sus comidas y los clientes que siguen
 * sus planes. Un plan ajeno responde 404, igual que uno que no existe.
 */
@Service
public class NutritionistPlanService {

    private final MealPlanRepository plans;
    private final MealPlanEnrollmentRepository enrollments;
    private final UserAccounts accounts;
    private final ClientProfiles clientProfiles;
    private final Clock clock;

    public NutritionistPlanService(
            MealPlanRepository plans,
            MealPlanEnrollmentRepository enrollments,
            UserAccounts accounts,
            ClientProfiles clientProfiles,
            Clock clock) {
        this.plans = plans;
        this.enrollments = enrollments;
        this.accounts = accounts;
        this.clientProfiles = clientProfiles;
        this.clock = clock;
    }

    public record PlanData(String title, String description, MealPlanGoal goal, int durationDays, Tier requiredTier) {}

    public record MealData(
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {}

    /** Un cliente que sigue alguno de los planes del nutricionista. */
    public record Client(
            UserAccount account, @Nullable ClientProfileView profile, List<String> planTitles, Instant since) {}

    @Transactional(readOnly = true)
    public Page<PlanCard> mine(UUID authorId, @Nullable PublicationStatus status, Pageable pageable) {
        Page<MealPlan> page = status == null
                ? plans.findByAuthorId(authorId, pageable)
                : plans.findByAuthorIdAndStatus(authorId, status, pageable);
        return page.map(NutritionistPlanService::card);
    }

    /** Cuántos planes tiene en cada estado (todos los estados, con cero si no hay). */
    @Transactional(readOnly = true)
    public Map<PublicationStatus, Long> countByStatus(UUID authorId) {
        Map<PublicationStatus, Long> counts = new EnumMap<>(PublicationStatus.class);
        for (PublicationStatus status : PublicationStatus.values()) {
            counts.put(status, 0L);
        }
        plans.countByStatus(authorId).forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return counts;
    }

    @Transactional(readOnly = true)
    public PlanCard get(UUID planId, UUID authorId) {
        return card(owned(planId, authorId));
    }

    @Transactional
    public PlanCard create(UUID authorId, PlanData data) {
        return card(plans.save(MealPlan.draft(
                authorId, data.title(), data.description(), data.goal(), data.durationDays(), data.requiredTier())));
    }

    @Transactional
    public PlanCard update(UUID planId, UUID authorId, PlanData data) {
        MealPlan plan = owned(planId, authorId);
        plan.update(data.title(), data.description(), data.goal(), data.durationDays(), data.requiredTier());
        return card(plan);
    }

    /** Solo se borran los borradores; un plan publicado se archiva (los clientes inscritos lo conservan). */
    @Transactional
    public void delete(UUID planId, UUID authorId) {
        MealPlan plan = owned(planId, authorId);
        if (!plan.isDraft()) {
            throw ApiException.conflict("plan-not-draft", "Solo se pueden borrar borradores: archiva el plan");
        }
        plans.delete(plan);
    }

    @Transactional
    public PlanCard publish(UUID planId, UUID authorId) {
        MealPlan plan = owned(planId, authorId);
        plan.publish(clock.instant());
        return card(plan);
    }

    @Transactional
    public PlanCard archive(UUID planId, UUID authorId) {
        MealPlan plan = owned(planId, authorId);
        plan.archive();
        return card(plan);
    }

    @Transactional
    public Meal addMeal(UUID planId, UUID authorId, MealData data) {
        MealPlan plan = owned(planId, authorId);
        Meal meal = plan.addMeal(
                data.dayNumber(), data.position(), data.category(), data.name(), data.description(), data.calories());
        plans.flush(); // persiste la comida (en cascada) y le asigna el id
        return meal;
    }

    @Transactional
    public Meal updateMeal(UUID planId, UUID mealId, UUID authorId, MealData data) {
        return owned(planId, authorId)
                .updateMeal(
                        mealId,
                        data.dayNumber(),
                        data.position(),
                        data.category(),
                        data.name(),
                        data.description(),
                        data.calories());
    }

    @Transactional
    public void removeMeal(UUID planId, UUID mealId, UUID authorId) {
        owned(planId, authorId).removeMeal(mealId);
    }

    /**
     * Clientes inscritos en sus planes (los más recientes primero), con los títulos de los planes que siguen y sus
     * datos físicos. {@code text} busca por nombre, apellidos o correo sin distinguir tildes. Todo en lote: unas pocas
     * consultas por página, tenga los clientes que tenga.
     */
    @Transactional(readOnly = true)
    public Page<Client> clients(UUID authorId, @Nullable String text, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<ClientSince> rows;
        if (text == null || text.isBlank()) {
            rows = enrollments.findClientsOf(authorId, page);
        } else {
            Set<UUID> matching = accounts.matching(enrollments.findClientIdsOf(authorId), text);
            if (matching.isEmpty()) {
                return Page.empty(page);
            }
            rows = enrollments.findClientsOfAmong(authorId, matching, page);
        }
        List<UUID> clientIds =
                rows.getContent().stream().map(ClientSince::getClientId).toList();
        if (clientIds.isEmpty()) {
            return new PageImpl<>(List.of(), page, rows.getTotalElements());
        }

        List<MealPlanEnrollment> theirs = enrollments.findByAuthorAndClientIdIn(authorId, clientIds);
        Map<UUID, String> titles = plans
                .findAllById(
                        theirs.stream().map(MealPlanEnrollment::getMealPlanId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(MealPlan::requireId, MealPlan::getTitle));
        Map<UUID, List<String>> planTitles = theirs.stream()
                .collect(Collectors.groupingBy(
                        MealPlanEnrollment::getClientId,
                        Collectors.mapping(e -> titles.getOrDefault(e.getMealPlanId(), ""), Collectors.toList())));
        Map<UUID, UserAccount> people = accounts.findAllById(clientIds);
        Map<UUID, ClientProfileView> profiles = clientProfiles.findAll(clientIds);
        return rows.map(row -> new Client(
                Objects.requireNonNull(people.get(row.getClientId())),
                profiles.get(row.getClientId()),
                planTitles.getOrDefault(row.getClientId(), List.of()),
                row.getSince()));
    }

    private static PlanCard card(MealPlan plan) {
        return PlanCard.of(plan, null, false);
    }

    private MealPlan owned(UUID planId, UUID authorId) {
        return plans.findByIdAndAuthorId(planId, authorId).orElseThrow(MealPlanService::notFound);
    }
}
