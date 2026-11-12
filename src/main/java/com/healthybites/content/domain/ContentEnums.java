package com.healthybites.content.domain;

/** Valores cerrados del módulo. El web-app los muestra en español. */
public final class ContentEnums {

    private ContentEnums() {}

    public enum ContentType {
        ARTICLE,
        VIDEO,
        GUIDE
    }

    public enum ContentCategory {
        NUTRITION,
        EXERCISE,
        MENTAL_HEALTH
    }

    public enum UnlockSource {
        REWARD,
        ADMIN
    }
}
