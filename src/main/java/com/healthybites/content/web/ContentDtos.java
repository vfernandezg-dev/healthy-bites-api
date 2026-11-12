package com.healthybites.content.web;

import com.healthybites.billing.Tier;
import com.healthybites.content.app.ContentService.ContentCard;
import com.healthybites.content.domain.ContentEnums.ContentCategory;
import com.healthybites.content.domain.ContentEnums.ContentType;
import com.healthybites.content.domain.ContentItem;
import com.healthybites.identity.PersonResponse;
import com.healthybites.shared.domain.PublicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class ContentDtos {

    private ContentDtos() {}

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
            PublicationStatus status,
            @Nullable PersonResponse author,
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
                    PersonResponse.of(card.author()),
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

    /**
     * Lo que ve el autor de un contenido: el detalle más los ids de la portada y del fichero, que tiene que reenviar
     * al editar (null los quita). Los clientes no los reciben: el fichero solo se abre con una URL firmada.
     */
    record EditableContentResponse(
            ContentSummaryResponse item,
            @Nullable String body,
            @Nullable UUID coverMediaId,
            @Nullable UUID fileMediaId) {

        static EditableContentResponse of(ContentCard card) {
            ContentItem item = card.item();
            return new EditableContentResponse(
                    ContentSummaryResponse.of(card), item.getBody(), item.getCoverMediaId(), item.getFileMediaId());
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
