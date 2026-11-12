package com.healthybites.nutrition.app;

import com.healthybites.billing.Tier;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.nutrition.app.MealPlanService.PlanCard;
import com.healthybites.nutrition.domain.Meal;
import com.healthybites.nutrition.domain.MealPlan;
import com.healthybites.nutrition.domain.MealPlanEnrollment;
import com.healthybites.nutrition.domain.MealPlanEnrollmentRepository;
import com.healthybites.nutrition.domain.MealPlanRepository;
import com.healthybites.nutrition.domain.NutritionEnums.MealCategory;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.profiles.ClientProfiles;
import com.healthybites.profiles.ClientProfiles.ClientProfileView;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
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
    public Page<PlanCard> mine(UUID authorId, Pageable pageable) {
        return plans.findByAuthorId(authorId, pageable).map(NutritionistPlanService::card);
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

    /** Clientes inscritos en sus planes, con los títulos de los planes que siguen y sus datos físicos. */
    @Transactional(readOnly = true)
    public List<Client> clients(UUID authorId) {
        List<MealPlanEnrollment> all = enrollments.findByAuthor(authorId);
        Map<UUID, String> titles = new LinkedHashMap<>();
        plans.findAllById(all.stream()
                        .map(MealPlanEnrollment::getMealPlanId)
                        .distinct()
                        .toList())
                .forEach(plan -> titles.put(plan.requireId(), plan.getTitle()));
        Map<UUID, List<MealPlanEnrollment>> byClient = new LinkedHashMap<>();
        all.forEach(e -> byClient.computeIfAbsent(e.getClientId(), id -> new ArrayList<>())
                .add(e));

        Map<UUID, UserAccount> people = accounts.findAllById(byClient.keySet());
        Map<UUID, ClientProfileView> profiles = clientProfiles.findAll(byClient.keySet());
        List<Client> result = new ArrayList<>();
        byClient.forEach((clientId, list) -> {
            UserAccount account = people.get(clientId);
            if (account != null) {
                result.add(new Client(
                        account,
                        profiles.get(clientId),
                        list.stream()
                                .map(e -> titles.getOrDefault(e.getMealPlanId(), ""))
                                .toList(),
                        list.stream()
                                .map(MealPlanEnrollment::getEnrolledAt)
                                .min(Instant::compareTo)
                                .orElse(clock.instant())));
            }
        });
        return result;
    }

    private static PlanCard card(MealPlan plan) {
        return PlanCard.of(plan, null, false);
    }

    private MealPlan owned(UUID planId, UUID authorId) {
        return plans.findByIdAndAuthorId(planId, authorId).orElseThrow(MealPlanService::notFound);
    }
}
