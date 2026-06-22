package cl.pymetrack.msnotificaciones.consumer;

import cl.pymetrack.msnotificaciones.service.NotificationTemplateService;
import cl.pymetrack.msnotificaciones.service.SendGridEmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PedidoEstadoConsumer {

    private final SendGridEmailService sendGridEmailService;
    private final NotificationTemplateService notificationTemplateService;

    public PedidoEstadoConsumer(
            SendGridEmailService sendGridEmailService,
            NotificationTemplateService notificationTemplateService
    ) {
        this.sendGridEmailService = sendGridEmailService;
        this.notificationTemplateService = notificationTemplateService;
    }

    @RabbitListener(queues = "pedido.estado.actualizado")
    public void consumirCambioEstado(Map<String, Object> event) {
        Long pedidoId = toLong(event.get("pedidoId"));
        Long pymeId = toLong(event.get("idPyme"));
        Long repartidorId = toLong(event.get("repartidorId"));

        String estadoAnterior = toStringSafe(event.get("estadoAnterior"));
        String estadoNuevo = toStringSafe(event.get("estadoNuevo"));
        String observacion = toStringSafe(event.get("observacion"));
        String fechaEvento = toStringSafe(event.get("fechaEvento"));

        System.out.println("======================================");
        System.out.println("📩 EVENTO RECIBIDO DESDE RABBITMQ");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("PYME ID: " + pymeId);
        System.out.println("Estado anterior: " + estadoAnterior);
        System.out.println("Estado nuevo: " + estadoNuevo);
        System.out.println("Repartidor ID: " + repartidorId);
        System.out.println("Observación: " + observacion);
        System.out.println("Fecha evento: " + fechaEvento);

        if (!debeNotificar(estadoNuevo)) {
            System.out.println("ℹ️ Estado sin correo al cliente: " + estadoNuevo);
            System.out.println("======================================");
            return;
        }

        String emailDestino = toStringSafe(event.get("emailCliente"));

        if (emailDestino.isBlank()) {
            emailDestino = System.getenv().getOrDefault(
                    "SENDGRID_TEST_TO_EMAIL",
                    "a.chavezs@duocuc.cl"
            );
        }

        String subject = notificationTemplateService.construirAsunto(
                estadoNuevo,
                pedidoId
        );

        String html = notificationTemplateService.construirHtml(
                pedidoId,
                estadoNuevo,
                observacion
        );

        System.out.println("📧 Enviando correo real con SendGrid a: " + emailDestino);

        boolean enviado = sendGridEmailService.enviarCorreo(
                emailDestino,
                subject,
                html
        );

        if (enviado) {
            System.out.println("✅ Correo enviado correctamente");
        } else {
            System.out.println("⚠️ No se pudo enviar el correo");
        }

        System.out.println("======================================");
    }

    private boolean debeNotificar(String estadoNuevo) {
        String estado = estadoNuevo == null ? "" : estadoNuevo.trim().toUpperCase();

        return estado.equals("ASIGNADO")
                || estado.equals("PEDIDO_RETIRADO")
                || estado.equals("EN_CAMINO")
                || estado.equals("ENTREGADO")
                || estado.equals("RECHAZADO")
                || estado.equals("CANCELADO");
    }

    private String toStringSafe(Object value) {
        return value == null ? "" : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}