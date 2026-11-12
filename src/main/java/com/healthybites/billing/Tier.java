package com.healthybites.billing;

/** Nivel de acceso. Cada nivel incluye los anteriores: VIP abre todo lo PREMIUM y lo BASIC. */
public enum Tier {
    BASIC,
    PREMIUM,
    VIP;

    /** ¿Este nivel da acceso a algo que requiere {@code required}? */
    public boolean includes(Tier required) {
        return compareTo(required) >= 0;
    }
}
