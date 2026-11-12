package com.healthybites.nutrition.app;

import com.healthybites.billing.Access;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.nutrition.MealPlanAccess;
import com.healthybites.nutrition.domain.MealPlan;
import com.healthybites.nutrition.domain.MealPlanEnrollment;
import com.healthybites.nutrition.domain.MealPlanEnrollmentRepository;
import com.healthybites.nutrition.domain.MealPlanRepository;
import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.shared.domain.PublicationStatus;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo de planes para los clientes, detalle con control de acceso e inscripciones. */
@Service
public class MealPlanService implements MealPlanAccess {

    private final MealPlanRepository plans;
    private final MealPlanEnrollmentRepository enrollments;
    private final PlanAccessPolicy access;
    private final UserAccounts accounts;
    private final Clock clock;

    public MealPlanService(
            MealPlanRepository plans,
            MealPlanEnrollmentRepository enrollments,
            PlanAccessPolicy access,
            UserAccounts accounts,
            Clock clock) {
        this.plans = plans;
        this.enrollments = enrollments;
        this.access = access;
        this.accounts = accounts;
        this.clock = clock;
    }

    /** Un plan con su autor y si el lector puede abrirlo. */
    public record PlanCard(MealPlan plan, @Nullable UserAccount author, boolean locked, int averageDailyCalories) {

        /** Se construye dentro de la transacción: las comidas se cargan aquí, no en la capa web. */
        public static PlanCard of(MealPlan plan, @Nullable UserAccount author, boolean locked) {
            return new PlanCard(plan, author, locked, plan.averageDailyCalories());
        }
    }

    public record Enrollment(MealPlanEnrollment enrollment, PlanCard plan) {}

    @Transactional(readOnly = true)
    public Page<PlanCard> catalog(UUID userId, boolean staff, @Nullable MealPlanGoal goal, Pageable pageable) {
        Page<MealPlan> page = goal == null
                ? plans.findByStatus(PublicationStatus.PUBLISHED, pageable)
                : plans.findByStatusAndGoal(PublicationStatus.PUBLISHED, goal, pageable);
        Access reader = access.reader(userId, staff);
        Map<UUID, UserAccount> authors = accounts.findAllById(
                page.getContent().stream().map(MealPlan::getAuthorId).distinct().toList());
        return page.map(
                plan -> PlanCard.of(plan, authors.get(plan.getAuthorId()), !PlanAccessPolicy.canOpen(reader, plan)));
    }

    /** Detalle con comidas: 403 {@code subscription-required} (con {@code requiredTier}) si no se puede abrir. */
    @Transactional(readOnly = true)
    public PlanCard open(UUID planId, UUID userId, boolean staff) {
        MealPlan plan = plans.findById(planId)
                .filter(candidate -> candidate.isVisibleTo(userId, staff))
                .orElseThrow(MealPlanService::notFound);
        access.requireOpen(plan, userId, staff);
        return PlanCard.of(plan, accounts.findById(plan.getAuthorId()).orElse(null), false);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> enrollments(UUID clientId) {
        List<MealPlanEnrollment> mine = enrollments.findByClientIdOrderByEnrolledAtDesc(clientId);
        Map<UUID, MealPlan> byId =
                plans
                        .findAllById(mine.stream()
                                .map(MealPlanEnrollment::getMealPlanId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(MealPlan::requireId, plan -> plan));
        Map<UUID, UserAccount> authors = accounts.findAllById(
                byId.values().stream().map(MealPlan::getAuthorId).distinct().toList());
        Access reader = access.reader(clientId, false);
        return mine.stream()
                .filter(enrollment -> byId.containsKey(enrollment.getMealPlanId()))
                .map(enrollment -> {
                    MealPlan plan = byId.get(enrollment.getMealPlanId());
                    return new Enrollment(
                            enrollment,
                            PlanCard.of(
                                    plan, authors.get(plan.getAuthorId()), !PlanAccessPolicy.canOpen(reader, plan)));
                })
                .toList();
    }

    /** Seguir un plan: hay que poder abrirlo. Repetirlo no duplica. */
    @Transactional
    public MealPlanEnrollment enroll(UUID planId, UUID clientId) {
        MealPlan plan = plans.findById(planId).filter(MealPlan::isPublished).orElseThrow(MealPlanService::notFound);
        access.requireOpen(plan, clientId, false);
        return enrollments
                .findByMealPlanIdAndClientId(planId, clientId)
                .orElseGet(() -> enrollments.save(
                        MealPlanEnrollment.of(planId, clientId, EnrollmentSource.SELF, clock.instant())));
    }

    /** Dejar de seguir un plan. Los desbloqueados con una recompensa se conservan (son un premio). */
    @Transactional
    public void unenroll(UUID planId, UUID clientId) {
        enrollments.findByMealPlanIdAndClientId(planId, clientId).ifPresent(enrollment -> {
            if (enrollment.getSource() == EnrollmentSource.REWARD) {
                throw ApiException.conflict(
                        "reward-enrollment", "Este plan lo desbloqueaste con una recompensa: no se puede quitar");
            }
            enrollments.delete(enrollment);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPublishedBy(UUID mealPlanId, UUID authorId) {
        return plans.isPublishedBy(mealPlanId, authorId);
    }

    @Override
    @Transactional
    public void grant(UUID mealPlanId, UUID clientId) {
        enrollments
                .findByMealPlanIdAndClientId(mealPlanId, clientId)
                .ifPresentOrElse(
                        MealPlanEnrollment::unlockByReward,
                        () -> enrollments.save(
                                MealPlanEnrollment.of(mealPlanId, clientId, EnrollmentSource.REWARD, clock.instant())));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> titlesOf(Collection<UUID> mealPlanIds) {
        return plans.findAllById(mealPlanIds).stream()
                .collect(Collectors.toMap(MealPlan::requireId, MealPlan::getTitle));
    }

    static ApiException notFound() {
        return ApiException.notFound("meal-plan-not-found", "El plan no existe");
    }
}
