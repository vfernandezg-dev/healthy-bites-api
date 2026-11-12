package com.healthybites.content.web;

import com.healthybites.content.app.ContentService;
import com.healthybites.content.app.ContentService.Filter;
import com.healthybites.content.domain.ContentEnums.ContentCategory;
import com.healthybites.content.domain.ContentEnums.ContentType;
import com.healthybites.content.web.ContentDtos.ContentDetailResponse;
import com.healthybites.content.web.ContentDtos.ContentSummaryResponse;
import com.healthybites.content.web.ContentDtos.FileLinkResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Contenido", description = "Catálogo de artículos, vídeos y guías")
class ContentController {

    private static final Set<String> SORTABLE = Set.of("publishedAt", "title");

    private final ContentService contents;

    ContentController(ContentService contents) {
        this.contents = contents;
    }

    @Operation(summary = "Contenido publicado, con filtros; locked indica si el usuario no puede abrirlo")
    @GetMapping("/content-items")
    PagedModel<ContentSummaryResponse> catalog(
            @RequestParam(required = false) @Nullable ContentType type,
            @RequestParam(required = false) @Nullable ContentCategory category,
            @RequestParam(required = false) @Nullable String q,
            @PageableDefault(sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(contents.catalog(
                        CurrentUser.id(),
                        isStaff(),
                        new Filter(type, category, q),
                        Sorting.restrict(pageable, SORTABLE))
                .map(ContentSummaryResponse::of));
    }

    @Operation(summary = "Detalle con el cuerpo (403 subscription-required, con requiredTier, si está bloqueado)")
    @GetMapping("/content-items/{slug}")
    ContentDetailResponse detail(@PathVariable String slug) {
        return ContentDetailResponse.of(contents.open(slug, CurrentUser.id(), isStaff()));
    }

    @Operation(summary = "Enlace firmado y temporal al fichero del contenido (vídeo, PDF...)")
    @GetMapping("/content-items/{id}/file")
    ResponseEntity<FileLinkResponse> file(@PathVariable UUID id) {
        String path = contents.fileLink(id, CurrentUser.id(), isStaff());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new FileLinkResponse(path, ContentService.FILE_LINK_TTL.toSeconds()));
    }

    @Operation(summary = "Contenido que he desbloqueado")
    @GetMapping("/me/content-unlocks")
    List<ContentSummaryResponse> unlocked() {
        return contents.unlockedBy(CurrentUser.id()).stream()
                .map(ContentSummaryResponse::of)
                .toList();
    }

    private static boolean isStaff() {
        return CurrentUser.hasRole("NUTRITIONIST") || CurrentUser.hasRole("ADMIN");
    }
}
