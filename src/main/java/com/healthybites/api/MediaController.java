package com.healthybites.api;

import com.healthybites.dto.UploadMediaDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.model.entity.MediaFile;
import com.healthybites.security.CurrentUser;
import com.healthybites.service.media.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Set;

@RequiredArgsConstructor
@RequestMapping("/media")
@RestController
public class MediaController {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_NUTRICIONISTA", "ROLE_ADMIN");

    private final MediaService mediaService;

    /** Sube un fichero (requiere sesión). Devuelve el nombre con el que se guarda, que es el que se referencia. */
    @PostMapping("/upload")
    public UploadMediaDTO upload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(authority -> STAFF_ROLES.contains(authority.getAuthority()));
        MediaFile media = mediaService.upload(file, CurrentUser.id(), staff);
        return new UploadMediaDTO(media.getFilename());
    }

    /**
     * Sirve un fichero registrado. Público porque {@code <img>} y {@code <video>} no pueden enviar el access token:
     * el nombre aleatorio hace de llave y el catálogo no lo entrega si el contenido está bloqueado.
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getResource(@PathVariable String filename) {
        MediaService.StoredMedia media = mediaService.open(filename)
                .orElseThrow(() -> new ResourceNotFoundException("Fichero no encontrado"));
        MediaFile file = media.file();
        ContentDisposition disposition = MediaService.isInline(file)
                ? ContentDisposition.inline().build()
                : ContentDisposition.attachment().filename(file.getFilename()).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                // El navegador no adivina el tipo ni ejecuta nada aunque el fichero se abra directamente.
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                // El contenido de un nombre no cambia nunca.
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate().immutable())
                .body(media.resource());
    }
}
