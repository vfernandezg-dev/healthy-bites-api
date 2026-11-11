package com.healthybites.api;

import com.healthybites.dto.ContactMessageDTO;
import com.healthybites.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    // Mensaje del formulario de contacto de la landing page (endpoint público)
    @PostMapping
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody ContactMessageDTO contactMessageDTO,
                                            HttpServletRequest request) {
        contactService.sendMessage(contactMessageDTO, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
