package com.healthybites.shared.web;

/** Cuántos elementos hay en cada estado de publicación (para los paneles, sin descargar las listas). */
public record PublicationSummaryResponse(long draft, long published, long archived) {}
