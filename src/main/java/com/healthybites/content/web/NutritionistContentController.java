package com.healthybites.content.web;

import com.healthybites.content.app.ContentService.ContentCard;
import com.healthybites.content.app.NutritionistContentService;
import com.healthybites.content.web.ContentDtos.ContentDetailResponse;
import com.healthybites.content.web.ContentDtos.ContentRequest;
import com.healthybites.content.web.ContentDtos.ContentSummaryResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nutritionist/content-items")
@Tag(name = "Nutricionista · Contenido", description = "Artículos, vídeos y guías propios")
class NutritionistContentController {

    private static final Set<String> SORTABLE = Set.of("createdAt", "updatedAt", "title", "status");

    private final NutritionistContentService contents;

    NutritionistContentController(NutritionistContentService contents) {
        this.contents = contents;
    }

    @Operation(summary = "Mi contenido (todos los estados)")
    @GetMapping
    PagedModel<ContentSummaryResponse> mine(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(contents.mine(CurrentUser.id(), Sorting.restrict(pageable, SORTABLE))
                .map(ContentSummaryResponse::of));
    }

    @Operation(summary = "Crea un borrador (portada y fichero: ids subidos antes con POST /media)")
    @PostMapping
    ResponseEntity<ContentDetailResponse> create(@Valid @RequestBody ContentRequest body) {
        ContentCard created = contents.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(URI.create(
                        "/nutritionist/content-items/" + created.item().requireId()))
                .body(ContentDetailResponse.of(created));
    }

    @GetMapping("/{id}")
    ContentDetailResponse get(@PathVariable UUID id) {
        return ContentDetailResponse.of(contents.get(id, CurrentUser.id()));
    }

    @PutMapping("/{id}")
    ContentDetailResponse update(@PathVariable UUID id, @Valid @RequestBody ContentRequest body) {
        return ContentDetailResponse.of(contents.update(id, CurrentUser.id(), body.toData()));
    }

    @Operation(summary = "Borra un borrador (lo publicado se archiva)")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        contents.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    ContentDetailResponse publish(@PathVariable UUID id) {
        return ContentDetailResponse.of(contents.publish(id, CurrentUser.id()));
    }

    @PostMapping("/{id}/archive")
    ContentDetailResponse archive(@PathVariable UUID id) {
        return ContentDetailResponse.of(contents.archive(id, CurrentUser.id()));
    }
}
