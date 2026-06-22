package cl.pymetrack.msnotificaciones.service;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendGridEmailServiceTest {

    @InjectMocks
    private SendGridEmailService emailService;

    @BeforeEach
    void setUp() {
        // Usamos ReflectionTestUtils para inyectar las variables @Value simulando el application.yml
        ReflectionTestUtils.setField(emailService, "enabled", true);
        ReflectionTestUtils.setField(emailService, "apiKey", "SG.mi-api-key-falsa-para-tests");
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@pymetrack.cl");
        ReflectionTestUtils.setField(emailService, "fromName", "PymeTrack");
    }

    // ==========================================
    // TESTS DE VALIDACIÓN (RAMAS TEMPRANAS)
    // ==========================================

    @Test
    void testEnviarCorreo_ServicioDesactivado() {
        ReflectionTestUtils.setField(emailService, "enabled", false);
        
        boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
        
        assertTrue(resultado); // Según el código, si está desactivado retorna true
    }

    @Test
    void testEnviarCorreo_SinApiKey() {
        ReflectionTestUtils.setField(emailService, "apiKey", "");
        
        boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
        
        assertFalse(resultado);
    }

    @Test
    void testEnviarCorreo_SinEmailOrigen() {
        ReflectionTestUtils.setField(emailService, "fromEmail", null);
        
        boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
        
        assertFalse(resultado);
    }

    // ==========================================
    // TESTS DE INTEGRACIÓN CON SENDGRID (MOCKED)
    // ==========================================

    @Test
    void testEnviarCorreo_EnvioExitoso() {
        // Interceptamos la creación de "new SendGrid()"
        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    // Preparamos una respuesta exitosa simulada de SendGrid (status 202)
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202); 
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
            
            assertTrue(resultado);
            // Verificamos que se instanció SendGrid exactamente 1 vez
            assertEquals(1, mockedSendGrid.constructed().size()); 
        }
    }

    @Test
    void testEnviarCorreo_FallaPorStatusHttp() {
        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    // Preparamos una respuesta de error simulada (ej. 400 Bad Request)
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(400); 
                    mockResponse.setBody("Bad Request");
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
            
            assertFalse(resultado);
        }
    }

    @Test
    void testEnviarCorreo_FallaPorExcepcionIO() {
        try (MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    // Simulamos que el servidor de SendGrid se cae o hay un timeout
                    when(mock.api(any(Request.class))).thenThrow(new IOException("Timeout de conexión"));
                })) {

            boolean resultado = emailService.enviarCorreo("cliente@test.com", "Asunto", "<h1>Hola</h1>");
            
            assertFalse(resultado);
        }
    }
}