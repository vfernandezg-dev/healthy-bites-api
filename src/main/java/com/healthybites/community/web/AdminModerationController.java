package com.healthybites.community.web;

import com.healthybites.community.app.ModerationService;
import com.healthybites.community.domain.ModerationReport;
import com.healthybites.community.web.CommunityDtos.ReportResponse;
import com.healthybites.community.web.CommunityDtos.ResolveRequest;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/moderation")
@Tag(name = "Admin · Moderación", description = "Denuncias de la comunidad (solo ADMIN)")
class AdminModerationController {

    private final ModerationService moderation;

    AdminModerationController(ModerationService moderation) {
        this.moderation = moderation;
    }

    @Operation(summary = "Denuncias por estado (por defecto, las abiertas)")
    @GetMapping("/reports")
    PagedModel<ReportResponse> reports(
            @RequestParam(defaultValue = "OPEN") ModerationReport.Status status,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                    Pageable pageable) {
        return new PagedModel<>(moderation
                .reports(status, Sorting.restrict(pageable, Set.of("createdAt")))
                .map(ReportResponse::of));
    }

    @Operation(summary = "Oculta lo denunciado (cierra todas sus denuncias) o descarta la denuncia")
    @PostMapping("/reports/{id}/resolve")
    ReportResponse resolve(@PathVariable UUID id, @Valid @RequestBody ResolveRequest body) {
        return ReportResponse.of(moderation.resolve(id, CurrentUser.id(), body.action(), body.note()));
    }
}
