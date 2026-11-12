package com.healthybites.content.web;

import com.healthybites.billing.Tier;
import com.healthybites.content.app.ContentService.ContentCard;
import com.healthybites.content.domain.ContentEnums.ContentCategory;
import com.healthybites.content.domain.ContentEnums.ContentStatus;
import com.healthybites.content.domain.ContentEnums.ContentType;
import com.healthybites.content.domain.ContentItem;
import com.healthybites.identity.UserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class ContentDtos {

    private ContentDtos() {}

    record AuthorResponse(UUID id, String name) {

        static @Nullable AuthorResponse of(@Nullable UserAccount account) {
            return account == null ? null : new AuthorResponse(account.id(), account.fullName());
        }
    }

    /** Ficha del catálogo, sin cuerpo ni fichero. {@code locked}: el lector no puede abrirlo. */
    record ContentSummaryResponse(
            UUID id,
            String slug,
            String title,
            String summary,
            ContentType type,
            ContentCategory category,
            Tier requiredTier,
            boolean locked,
            @Nullable String coverPath,
            boolean hasFile,
            ContentStatus status,
            @Nullable AuthorResponse author,
            @Nullable Instant publishedAt) {

        static ContentSummaryResponse of(ContentCard card) {
            ContentItem item = card.item();
            return new ContentSummaryResponse(
                    item.requireId(),
                    item.getSlug(),
                    item.getTitle(),
                    item.getSummary(),
                    item.getType(),
                    item.getCategory(),
                    item.getRequiredTier(),
                    card.locked(),
                    card.coverPath(),
                    item.getFileMediaId() != null,
                    item.getStatus(),
                    AuthorResponse.of(card.author()),
                    item.getPublishedAt());
        }
    }

    record ContentDetailResponse(
            ContentSummaryResponse item, @Nullable String body) {

        static ContentDetailResponse of(ContentCard card) {
            return new ContentDetailResponse(
                    ContentSummaryResponse.of(card), card.item().getBody());
        }
    }

    /** Ruta firmada (relativa a la API) y de vida corta al fichero. */
    record FileLinkResponse(String path, long expiresInSeconds) {}

    /** El autor edita también los ids de la portada y del fichero (subidos antes con {@code POST /media}). */
    record ContentRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String title,

            @NotBlank(message = "El resumen es obligatorio") @Size(max = 500, message = "Máximo 500 caracteres") String summary,

            @Size(max = 20000, message = "Máximo 20000 caracteres") @Nullable String body,

            @NotNull(message = "Elige el tipo") ContentType type,
            @NotNull(message = "Elige la categoría") ContentCategory category,
            @NotNull(message = "Elige el nivel requerido") Tier requiredTier,
            @Nullable UUID coverMediaId,
            @Nullable UUID fileMediaId) {

        ContentItem.Data toData() {
            return new ContentItem.Data(title, summary, body, type, category, requiredTier, coverMediaId, fileMediaId);
        }
    }
}
