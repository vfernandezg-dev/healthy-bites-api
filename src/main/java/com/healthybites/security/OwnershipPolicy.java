package com.healthybites.security;

import com.healthybites.model.enums.EstadoPago;
import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Reglas de propiedad para las expresiones {@code @PreAuthorize} de los controladores, por ejemplo
 * {@code @PreAuthorize("hasRole('ADMIN') or @owner.isClient(#clienteId)")}.
 *
 * <p>La identidad sale siempre del access token ({@link CurrentUser}); los ids de la ruta o del cuerpo solo se
 * comparan con ella. Antes los servicios confiaban en ellos y cualquier usuario podía leer o modificar datos ajenos.
 */
@Component("owner")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnershipPolicy {

    private static final List<TipoSuscripcion> PREMIUM_TIERS = List.of(TipoSuscripcion.PREMIUM, TipoSuscripcion.VIP);

    private final EntityManager em;

    // ------------------------------------------------------------------ identidad

    public boolean isUser(Integer usuarioId) {
        return usuarioId != null && usuarioId.equals(CurrentUser.id());
    }

    public boolean isClient(Integer clienteId) {
        return clienteId != null && exists(
                "select count(c) from Cliente c where c.id = :id and c.usuario.id = :uid", clienteId);
    }

    public boolean isNutritionist(Integer nutricionistaId) {
        return nutricionistaId != null && exists(
                "select count(n) from Nutricionista n where n.id = :id and n.usuario.id = :uid", nutricionistaId);
    }

    // ------------------------------------------------------------------ recursos del cliente

    public boolean ownsMeta(Integer metaId) {
        return metaId != null && exists(
                "select count(m) from Meta m where m.id = :id and m.cliente.usuario.id = :uid", metaId);
    }

    public boolean ownsHabito(Integer habitoId) {
        return habitoId != null && exists(
                "select count(h) from Habito h where h.id = :id and h.cliente.usuario.id = :uid", habitoId);
    }

    /** El seguimiento pertenece a esa meta y la meta es del usuario. */
    public boolean ownsSeguimiento(Integer metaId, Integer seguimientoId) {
        return metaId != null && seguimientoId != null && em.createQuery(
                        "select count(s) from Seguimiento s where s.id = :id and s.meta.id = :metaId "
                                + "and s.meta.cliente.usuario.id = :uid", Long.class)
                .setParameter("id", seguimientoId)
                .setParameter("metaId", metaId)
                .setParameter("uid", CurrentUser.id())
                .getSingleResult() > 0;
    }

    public boolean ownsSuscripcion(Integer suscripcionId) {
        return suscripcionId != null && exists(
                "select count(s) from Suscripcion s where s.id = :id and s.usuario.id = :uid", suscripcionId);
    }

    // ------------------------------------------------------------------ comunidad

    public boolean ownsGrupo(Integer grupoId) {
        return grupoId != null && exists(
                "select count(g) from Grupo g where g.id = :id and g.cliente.usuario.id = :uid", grupoId);
    }

    /** Los grupos públicos los lee cualquiera; los privados, solo su creador y sus miembros. */
    public boolean canReadGrupo(Integer grupoId) {
        return grupoId != null && exists("""
                select count(g) from Grupo g left join g.cliente gc
                where g.id = :id and (g.esPrivado = false or gc.usuario.id = :uid
                    or exists (select u from Unirse u where u.grupo = g and u.cliente.usuario.id = :uid))""",
                grupoId);
    }

    /** Solo se puede entrar por cuenta propia en grupos públicos. */
    public boolean canJoinGrupo(Integer grupoId) {
        return grupoId != null && em.createQuery(
                        "select count(g) from Grupo g where g.id = :id and g.esPrivado = false", Long.class)
                .setParameter("id", grupoId)
                .getSingleResult() > 0;
    }

    public boolean ownsPublicacion(Integer publicacionId) {
        return publicacionId != null && exists(
                "select count(p) from Publicacion p where p.id = :id and p.cliente.usuario.id = :uid", publicacionId);
    }

    public boolean canReadPublicacion(Integer publicacionId) {
        return publicacionId != null && exists("""
                select count(p) from Publicacion p left join p.grupo g left join g.cliente gc
                where p.id = :id and (g is null or g.esPrivado = false or gc.usuario.id = :uid
                    or exists (select u from Unirse u where u.grupo = g and u.cliente.usuario.id = :uid))""",
                publicacionId);
    }

    /** El comentario pertenece a esa publicación y lo escribió el usuario. */
    public boolean ownsComentario(Integer publicacionId, Integer comentarioId) {
        return publicacionId != null && comentarioId != null && em.createQuery(
                        "select count(c) from Comentario c where c.id = :id and c.publicacion.id = :pid "
                                + "and c.cliente.usuario.id = :uid", Long.class)
                .setParameter("id", comentarioId)
                .setParameter("pid", publicacionId)
                .setParameter("uid", CurrentUser.id())
                .getSingleResult() > 0;
    }

    // ------------------------------------------------------------------ contenido y planes del nutricionista

    public boolean ownsPlan(Integer planId) {
        return planId != null && exists(
                "select count(p) from PlanAlimenticio p where p.id = :id and p.nutricionista.usuario.id = :uid", planId);
    }

    /** La comida pertenece a ese plan (sin esto, con un plan gratuito se podían leer comidas de otro). */
    public boolean comidaInPlan(Integer planId, Integer comidaId) {
        return planId != null && comidaId != null && em.createQuery(
                        "select count(c) from ComidaDiaria c where c.id = :id and c.planAlimenticio.id = :planId",
                        Long.class)
                .setParameter("id", comidaId)
                .setParameter("planId", planId)
                .getSingleResult() > 0;
    }

    /** La comida pertenece a ese plan y el plan es del nutricionista. */
    public boolean ownsComida(Integer planId, Integer comidaId) {
        return ownsPlan(planId) && comidaInPlan(planId, comidaId);
    }

    public boolean ownsContenido(Integer contenidoId) {
        return contenidoId != null && exists(
                "select count(c) from Contenido c where c.id = :id and c.nutricionista.usuario.id = :uid", contenidoId);
    }

    public boolean ownsRecompensa(Integer recompensaId) {
        return recompensaId != null && exists(
                "select count(r) from Recompensa r where r.id = :id and r.nutricionista.usuario.id = :uid", recompensaId);
    }

    // ------------------------------------------------------------------ acceso premium (en el servidor)

    /** Suscripción PREMIUM o VIP pagada. Sin suscripción, o sin pagar, no hay acceso premium. */
    public boolean isPremium() {
        return em.createQuery("""
                        select count(s) from Suscripcion s where s.usuario.id = :uid
                            and s.estadoPago = :pagado and s.tipoSuscripcion in :tiers""", Long.class)
                .setParameter("uid", CurrentUser.id())
                .setParameter("pagado", EstadoPago.PAGADO)
                .setParameter("tiers", PREMIUM_TIERS)
                .getSingleResult() > 0;
    }

    /** Contenido gratuito, desbloqueado (por ejemplo, con una recompensa) o con suscripción premium pagada. */
    public boolean canReadContenido(Integer contenidoId) {
        if (contenidoId == null) {
            return false;
        }
        boolean freeOrUnlocked = exists("""
                select count(c) from Contenido c where c.id = :id and (c.esGratis = true
                    or exists (select a from AccesoContenido a where a.contenido = c and a.cliente.usuario.id = :uid))""",
                contenidoId);
        return freeOrUnlocked || (isPremium() && found("select count(c) from Contenido c where c.id = :id", contenidoId));
    }

    /** Plan gratuito, desbloqueado o con suscripción premium pagada. */
    public boolean canReadPlan(Integer planId) {
        if (planId == null) {
            return false;
        }
        boolean freeOrUnlocked = exists("""
                select count(p) from PlanAlimenticio p where p.id = :id and (p.esGratis = true
                    or exists (select a from AccesoPlan a where a.planAlimenticio = p and a.cliente.usuario.id = :uid))""",
                planId);
        return freeOrUnlocked || (isPremium() && found("select count(p) from PlanAlimenticio p where p.id = :id", planId));
    }

    /** Contenidos de pago que el usuario todavía no puede abrir (ninguno con suscripción premium pagada). */
    public Set<Integer> lockedContenidoIds() {
        if (isPremium()) {
            return Set.of();
        }
        return Set.copyOf(em.createQuery("""
                        select c.id from Contenido c where c.esGratis = false and not exists (
                            select a from AccesoContenido a where a.contenido = c and a.cliente.usuario.id = :uid)""",
                        Integer.class)
                .setParameter("uid", CurrentUser.id())
                .getResultList());
    }

    /** El recurso existe (sin mirar quién es el dueño). */
    private boolean found(String countQuery, Integer id) {
        return em.createQuery(countQuery, Long.class).setParameter("id", id).getSingleResult() > 0;
    }

    private boolean exists(String countQuery, Integer id) {
        return em.createQuery(countQuery, Long.class)
                .setParameter("id", id)
                .setParameter("uid", CurrentUser.id())
                .getSingleResult() > 0;
    }
}
