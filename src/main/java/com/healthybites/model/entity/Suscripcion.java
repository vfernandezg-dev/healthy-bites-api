package com.healthybites.model.entity;

import com.healthybites.model.enums.EstadoPago;
import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "suscripcion")
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suscripcion", nullable = false)
    private TipoSuscripcion tipoSuscripcion;

    @Column(name = "precio", nullable = false)
    private double precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_suscripcion_usuario"))
    private Usuario usuario;

    /** Da acceso al contenido y a los planes de pago: PREMIUM o VIP, y pagada. */
    public boolean isPremiumActiva() {
        return estadoPago == EstadoPago.PAGADO
                && (tipoSuscripcion == TipoSuscripcion.PREMIUM || tipoSuscripcion == TipoSuscripcion.VIP);
    }

    /** Null-safe: sin suscripción no hay acceso premium. */
    public static boolean isPremiumActiva(Suscripcion suscripcion) {
        return suscripcion != null && suscripcion.isPremiumActiva();
    }
}
