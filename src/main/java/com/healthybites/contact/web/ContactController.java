package com.healthybites.contact.web;

import com.healthybites.contact.app.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
@Tag(name = "Contacto", description = "Formulario de contacto de la landing")
@SecurityRequirements
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Envía un mensaje al equipo (204, 422, 429 o 503)")
    @PostMapping
    ResponseEntity<Void> sendMessage(@Valid @RequestBody ContactMessage message, HttpServletRequest request) {
        contactService.send(
                new ContactService.Message(
                        message.firstName(), message.lastName(), message.email(), message.message(), message.website()),
                request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
