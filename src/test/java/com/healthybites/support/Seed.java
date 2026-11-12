package com.healthybites.support;

import java.util.UUID;

/** Cuentas del seed de demostración (db/seed-dev/R__dev_seed.sql). Contraseña de todas: {@link #PASSWORD}. */
public final class Seed {

    public static final String PASSWORD = "healthy123";

    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-7000-8000-000000000001");
    public static final UUID CARLOS_ID = UUID.fromString("00000000-0000-7000-8000-000000000002");
    public static final UUID ELENA_ID = UUID.fromString("00000000-0000-7000-8000-000000000003");
    public static final UUID JUAN_ID = UUID.fromString("00000000-0000-7000-8000-000000000004");
    public static final UUID MARIA_ID = UUID.fromString("00000000-0000-7000-8000-000000000005");
    public static final UUID PEDRO_ID = UUID.fromString("00000000-0000-7000-8000-000000000006");

    public static final String ADMIN = "admin@healthybites.com";
    public static final String CARLOS = "carlos.garcia@healthybites.com";
    public static final String ELENA = "elena.gomez@healthybites.com";
    public static final String JUAN = "juan.perez@healthybites.com";
    public static final String MARIA = "maria.lopez@healthybites.com";
    public static final String PEDRO = "pedro.gomez@healthybites.com";

    private Seed() {}
}
