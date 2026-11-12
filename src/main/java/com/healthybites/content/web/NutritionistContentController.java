package com.healthybites.content.web;

import com.healthybites.content.app.ContentService.ContentCard;
import com.healthybites.content.app.NutritionistContentService;
import com.healthybites.content.web.ContentDtos.ContentRequest;
import com.healthybites.content.web.ContentDtos.ContentSummaryResponse;
import com.healthybites.content.web.ContentDtos.EditableContentResponse;
import com.healthybites.shared.domain.PublicationStatus;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.PublicationSummaryResponse;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "Mi contenido (todos los estados, o solo uno con ?status=)")
    @GetMapping
    PagedModel<ContentSummaryResponse> mine(
            @RequestParam(required = false) @Nullable PublicationStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(contents.mine(CurrentUser.id(), status, Sorting.restrict(pageable, SORTABLE))
                .map(ContentSummaryResponse::of));
    }

    @Operation(summary = "Cuántos contenidos tengo en cada estado")
    @GetMapping("/summary")
    PublicationSummaryResponse summary() {
        Map<PublicationStatus, Long> counts = contents.countByStatus(CurrentUser.id());
        return new PublicationSummaryResponse(
                counts.get(PublicationStatus.DRAFT),
                counts.get(PublicationStatus.PUBLISHED),
                counts.get(PublicationStatus.ARCHIVED));
    }

    @Operation(summary = "Crea un borrador (portada y fichero: ids subidos antes con POST /media)")
    @PostMapping
    ResponseEntity<EditableContentResponse> create(@Valid @RequestBody ContentRequest body) {
        ContentCard created = contents.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(Locations.of(
                        "/nutritionist/content-items/{id}", created.item().requireId()))
                .body(EditableContentResponse.of(created));
    }

    @GetMapping("/{id}")
    EditableContentResponse get(@PathVariable UUID id) {
        return EditableContentResponse.of(contents.get(id, CurrentUser.id()));
    }

    @PutMapping("/{id}")
    EditableContentResponse update(@PathVariable UUID id, @Valid @RequestBody ContentRequest body) {
        return EditableContentResponse.of(contents.update(id, CurrentUser.id(), body.toData()));
    }

    @Operation(
            summary = "Borra un borrador",
            description = "Lo publicado no se borra (409): se retira con POST /{id}/archive.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        contents.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    EditableContentResponse publish(@PathVariable UUID id) {
        return EditableContentResponse.of(contents.publish(id, CurrentUser.id()));
    }

    @PostMapping("/{id}/archive")
    EditableContentResponse archive(@PathVariable UUID id) {
        return EditableContentResponse.of(contents.archive(id, CurrentUser.id()));
    }
}
