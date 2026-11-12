package com.healthybites.content.domain;

import java.text.Normalizer;
import java.util.Locale;

/** Slugs legibles para las URLs: «Guía de alimentación» → {@code guia-de-alimentacion}. */
public final class Slugs {

    private static final int MAX_LENGTH = 120;

    private Slugs() {}

    public static String of(String title) {
        String ascii = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String slug =
                ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.isEmpty() ? "contenido" : slug;
    }
}
