package com.healthybites.content.app;

import com.healthybites.content.app.ContentService.ContentCard;
import com.healthybites.content.domain.ContentEnums.ContentStatus;
import com.healthybites.content.domain.ContentItem;
import com.healthybites.content.domain.ContentItemRepository;
import com.healthybites.content.domain.Slugs;
import com.healthybites.media.MediaFiles;
import com.healthybites.media.MediaPurpose;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contenido del nutricionista: borradores, publicación y archivo. La portada y el fichero tienen que ser ficheros
 * subidos por el propio autor con el propósito correcto. Lo ajeno responde 404.
 */
@Service
public class NutritionistContentService {

    private final ContentItemRepository items;
    private final ContentService contents;
    private final MediaFiles media;
    private final Clock clock;

    public NutritionistContentService(
            ContentItemRepository items, ContentService contents, MediaFiles media, Clock clock) {
        this.items = items;
        this.contents = contents;
        this.media = media;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<ContentCard> mine(UUID authorId, @Nullable ContentStatus status, Pageable pageable) {
        Page<ContentItem> page = status == null
                ? items.findByAuthorId(authorId, pageable)
                : items.findByAuthorIdAndStatus(authorId, status, pageable);
        return page.map(item -> contents.card(item, null, false));
    }

    /** Cuántos contenidos tiene en cada estado (todos los estados, con cero si no hay). */
    @Transactional(readOnly = true)
    public Map<ContentStatus, Long> countByStatus(UUID authorId) {
        Map<ContentStatus, Long> counts = new EnumMap<>(ContentStatus.class);
        for (ContentStatus status : ContentStatus.values()) {
            counts.put(status, 0L);
        }
        items.countByStatus(authorId).forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return counts;
    }

    @Transactional(readOnly = true)
    public ContentCard get(UUID itemId, UUID authorId) {
        return contents.card(owned(itemId, authorId), null, false);
    }

    @Transactional
    public ContentCard create(UUID authorId, ContentItem.Data data) {
        checkMedia(authorId, data);
        return contents.card(items.save(ContentItem.draft(authorId, uniqueSlug(data.title()), data)), null, false);
    }

    @Transactional
    public ContentCard update(UUID itemId, UUID authorId, ContentItem.Data data) {
        checkMedia(authorId, data);
        ContentItem item = owned(itemId, authorId);
        item.update(data);
        return contents.card(item, null, false);
    }

    @Transactional
    public void delete(UUID itemId, UUID authorId) {
        ContentItem item = owned(itemId, authorId);
        if (!item.isDraft()) {
            throw ApiException.conflict("content-not-draft", "Solo se pueden borrar borradores: archiva el contenido");
        }
        items.delete(item);
    }

    @Transactional
    public ContentCard publish(UUID itemId, UUID authorId) {
        ContentItem item = owned(itemId, authorId);
        item.publish(clock.instant());
        return contents.card(item, null, false);
    }

    @Transactional
    public ContentCard archive(UUID itemId, UUID authorId) {
        ContentItem item = owned(itemId, authorId);
        item.archive();
        return contents.card(item, null, false);
    }

    private void checkMedia(UUID authorId, ContentItem.Data data) {
        if (data.coverMediaId() != null) {
            media.requireOwned(data.coverMediaId(), authorId, MediaPurpose.CONTENT_COVER);
        }
        if (data.fileMediaId() != null) {
            media.requireOwned(data.fileMediaId(), authorId, MediaPurpose.CONTENT_FILE);
        }
    }

    private String uniqueSlug(String title) {
        String base = Slugs.of(title);
        String slug = base;
        for (int n = 2; items.existsBySlug(slug); n++) {
            slug = base + "-" + n;
        }
        return slug;
    }

    private ContentItem owned(UUID itemId, UUID authorId) {
        return items.findByIdAndAuthorId(itemId, authorId).orElseThrow(ContentService::notFound);
    }
}
