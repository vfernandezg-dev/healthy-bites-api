package com.healthybites.media.web;

import com.healthybites.identity.Role;
import com.healthybites.media.MediaPurpose;
import com.healthybites.media.app.MediaService;
import com.healthybites.media.app.MediaService.StoredMedia;
import com.healthybites.media.domain.MediaFile;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/media")
@Tag(name = "Ficheros", description = "Subida de imágenes y ficheros de contenido")
class MediaController {

    private final MediaService media;

    MediaController(MediaService media) {
        this.media = media;
    }

    record UploadResponse(
            UUID id,
            String contentType,
            long sizeBytes,
            @Nullable String path) {}

    @Operation(
            summary = "Sube un fichero",
            description = "El tipo se detecta por su contenido. POST_IMAGE: cualquier usuario, solo imágenes. "
                    + "CONTENT_COVER y CONTENT_FILE: nutricionistas. Los CONTENT_FILE no tienen ruta pública: se "
                    + "abren desde el contenido.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UploadResponse> upload(@RequestPart("file") MultipartFile file, @RequestParam MediaPurpose purpose) {
        MediaFile stored = media.upload(file, purpose, CurrentUser.id(), currentRole());
        UUID id = stored.requireId();
        UploadResponse body = new UploadResponse(
                id, stored.getContentType(), stored.getSizeBytes(), stored.isPublic() ? media.publicPath(id) : null);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(id)
                        .toUri())
                .body(body);
    }

    @Operation(summary = "Lee un fichero: los públicos por id; los protegidos, solo con su URL firmada")
    @SecurityRequirements
    @GetMapping("/{id}")
    ResponseEntity<Resource> read(
            @PathVariable UUID id,
            @RequestParam(required = false) @Nullable Long expires,
            @RequestParam(required = false) @Nullable String signature) {
        StoredMedia stored = media.open(id, expires, signature)
                .orElseThrow(() -> ApiException.notFound("media-not-found", "Fichero no encontrado"));
        MediaFile file = stored.file();
        ContentDisposition disposition = file.isInline()
                ? ContentDisposition.inline().build()
                : ContentDisposition.attachment().filename(file.getStorageKey()).build();
        CacheControl cache = file.isPublic()
                // El contenido de un id no cambia nunca.
                ? CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable()
                : CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                // El navegador no adivina el tipo ni ejecuta nada aunque el fichero se abra directamente.
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                .cacheControl(cache)
                .body(stored.resource());
    }

    private static Role currentRole() {
        return Arrays.stream(Role.values())
                .filter(role -> CurrentUser.hasRole(role.name()))
                .findFirst()
                .orElseThrow();
    }
}
