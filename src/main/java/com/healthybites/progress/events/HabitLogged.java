package com.healthybites.progress.events;

import java.time.LocalDate;
import java.util.UUID;

/** El cliente registró sus hábitos de un día (cuenta para las rachas). */
public record HabitLogged(UUID clientId, LocalDate loggedOn) {}
