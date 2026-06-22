package cl.pymetrack.msnotificaciones.consumer;

import cl.pymetrack.msnotificaciones.service.NotificationTemplateService;
import cl.pymetrack.msnotificaciones.service.SendGridEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoEstadoConsumerTest {

    @Mock
    private SendGridEmailService sendGridEmailService;

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @InjectMocks
    private PedidoEstadoConsumer consumer;

    private Map<String, Object> event;

    @BeforeEach
    void setUp() {
        event = new HashMap<>();
        // Configuramos un evento base estándar
        event.put("pedidoId", 100L);
        event.put("idPyme", 10L);
        event.put("repartidorId", 5L);
        event.put("estadoAnterior", "PREPARANDO");
        event.put("observacion", "Sin novedad");
        event.put("fechaEvento", "2026-06-22T10:00:00");
        event.put("emailCliente", "cliente@test.com");
    }

    // ==========================================
    // TESTS: RAMAS DE "debeNotificar" (FALSAS)
    // ==========================================

    @Test
    void testConsumir_EstadoNoNotificable() {
        // "CREADO" no está en la lista de estados que envían correo
        event.put("estadoNuevo", "CREADO");

        consumer.consumirCambioEstado(event);

        // Verificamos que se abortó la operación temprano y no se llamó a los servicios
        verify(notificationTemplateService, never()).construirAsunto(anyString(), anyLong());
        verify(sendGridEmailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    void testConsumir_EstadoNullOBlanco() {
        // Probamos con estado null (cubre la validación estadoNuevo == null)
        event.put("estadoNuevo", null);
        consumer.consumirCambioEstado(event);

        // Probamos con estado en blanco
        event.put("estadoNuevo", "   ");
        consumer.consumirCambioEstado(event);

        verify(sendGridEmailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    // ==========================================
    // TESTS: CAMINOS DE ÉXITO Y FALLO DE ENVÍO
    // ==========================================

    @Test
    void testConsumir_EnvioExitoso() {
        event.put("estadoNuevo", "ENTREGADO"); // Estado válido

        when(notificationTemplateService.construirAsunto("ENTREGADO", 100L)).thenReturn("Asunto Test");
        when(notificationTemplateService.construirHtml(100L, "ENTREGADO", "Sin novedad")).thenReturn("<p>Html Test</p>");
        when(sendGridEmailService.enviarCorreo("cliente@test.com", "Asunto Test", "<p>Html Test</p>")).thenReturn(true);

        consumer.consumirCambioEstado(event);

        verify(sendGridEmailService, times(1)).enviarCorreo("cliente@test.com", "Asunto Test", "<p>Html Test</p>");
    }

    @Test
    void testConsumir_FalloEnEnvio() {
        event.put("estadoNuevo", "RECHAZADO"); // Estado válido

        when(notificationTemplateService.construirAsunto("RECHAZADO", 100L)).thenReturn("Asunto Test");
        when(notificationTemplateService.construirHtml(100L, "RECHAZADO", "Sin novedad")).thenReturn("<p>Html Test</p>");
        // Simulamos que el envío falló para cubrir el "else" del if(enviado)
        when(sendGridEmailService.enviarCorreo("cliente@test.com", "Asunto Test", "<p>Html Test</p>")).thenReturn(false);

        consumer.consumirCambioEstado(event);

        verify(sendGridEmailService, times(1)).enviarCorreo(anyString(), anyString(), anyString());
    }

    // ==========================================
    // TESTS: CASOS EXTREMOS (toLong y Fallback Email)
    // ==========================================

    @Test
    void testConsumir_EmailDestinoEnBlanco_UsaFallback() {
        event.put("estadoNuevo", "ASIGNADO"); // Estado válido
        event.put("emailCliente", ""); // Forzamos el uso del fallback del System.getenv()

        when(notificationTemplateService.construirAsunto(anyString(), anyLong())).thenReturn("Asunto");
        when(notificationTemplateService.construirHtml(anyLong(), anyString(), anyString())).thenReturn("HTML");
        
        consumer.consumirCambioEstado(event);

        // Verificamos que se intentó enviar al correo por defecto configurado en tu código
        verify(sendGridEmailService, times(1)).enviarCorreo(eq("a.chavezs@duocuc.cl"), anyString(), anyString());
    }

    @Test
    void testConsumir_VerificacionCompletaDeEstadosParaBranchCoverage() {
        // Para asegurar el 100% de ramas lógicas, pasamos por todos los estados válidos
        String[] estadosValidos = {"ASIGNADO", "PEDIDO_RETIRADO", "EN_CAMINO", "ENTREGADO", "RECHAZADO", "CANCELADO"};
        
        when(notificationTemplateService.construirAsunto(anyString(), anyLong())).thenReturn("A");
        when(notificationTemplateService.construirHtml(anyLong(), anyString(), anyString())).thenReturn("B");

        for (String estado : estadosValidos) {
            event.put("estadoNuevo", estado);
            consumer.consumirCambioEstado(event);
        }

        verify(sendGridEmailService, times(estadosValidos.length)).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    void testConsumir_PruebasMetodoToLong() {
        event.put("estadoNuevo", "CANCELADO"); // Para que pase al código principal
        
        // Probamos los distintos tipos de datos que puede recibir "toLong"
        event.put("pedidoId", 50); // Integer (instancia de Number)
        event.put("idPyme", "20"); // String válido
        event.put("repartidorId", "no-es-numero"); // String inválido (arroja NumberFormatException interno)
        
        when(notificationTemplateService.construirAsunto(anyString(), anyLong())).thenReturn("A");
        when(notificationTemplateService.construirHtml(any(), anyString(), anyString())).thenReturn("B");

        // Al ejecutar, toLong no debe lanzar error que detenga la aplicación, debe manejarlo silenciosamente
        consumer.consumirCambioEstado(event);

        // Verificamos que llegó al final de la ejecución sin caerse
        verify(sendGridEmailService, times(1)).enviarCorreo(anyString(), anyString(), anyString());
    }
}