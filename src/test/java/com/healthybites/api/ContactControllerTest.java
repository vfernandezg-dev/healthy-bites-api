package com.healthybites.api;

import com.healthybites.dto.ContactMessageDTO;
import com.healthybites.exception.GlobalExceptionHandler;
import com.healthybites.exception.TooManyRequestsException;
import com.healthybites.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private static final String VALID_BODY = """
            {
              "nombre": "Ana",
              "apellido": "Torres",
              "correo": "ana@correo.com",
              "mensaje": "Quisiera más información sobre los planes.",
              "website": ""
            }
            """;

    @Mock
    private ContactService contactService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ContactController(contactService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void responde204YDelegaEnElServicioConLaIpDelCliente() throws Exception {
        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.7");
                            return request;
                        }))
                .andExpect(status().isNoContent());

        ArgumentCaptor<ContactMessageDTO> captor = ArgumentCaptor.forClass(ContactMessageDTO.class);
        verify(contactService).sendMessage(captor.capture(), eq("203.0.113.7"));
        assertThat(captor.getValue().getCorreo()).isEqualTo("ana@correo.com");
    }

    @Test
    void responde422CuandoLosDatosNoSonValidos() throws Exception {
        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "A", "apellido": "", "correo": "no-es-correo", "mensaje": "corto"}
                                """))
                .andExpect(status().isUnprocessableContent());

        verifyNoInteractions(contactService);
    }

    @Test
    void responde429CuandoSeSuperaElLimite() throws Exception {
        doThrow(new TooManyRequestsException("Demasiados mensajes"))
                .when(contactService).sendMessage(any(), anyString());

        mockMvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Demasiados mensajes"));
    }

    @Test
    void responde503SinExponerDetallesCuandoFallaElCorreo() throws Exception {
        doThrow(new MailSendException("535 Authentication failed for smtp.gmail.com"))
                .when(contactService).sendMessage(any(), anyString());

        mockMvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("No pudimos enviar el correo en este momento. Inténtalo más tarde."));
    }
}
