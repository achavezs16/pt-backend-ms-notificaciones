package cl.pymetrack.msnotificaciones.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationTemplateService {

    public String construirAsunto(String estadoNuevo, Long pedidoId) {
        String estado = normalizarEstado(estadoNuevo);

        return switch (estado) {
            case "ASIGNADO" -> "Tu pedido #" + pedidoId + " fue asignado";
            case "EN_CAMINO" -> "Tu pedido #" + pedidoId + " está en camino";
            case "ENTREGADO" -> "Tu pedido #" + pedidoId + " fue entregado";
            case "RECHAZADO" -> "Actualización de tu pedido #" + pedidoId;
            case "CANCELADO" -> "Tu pedido #" + pedidoId + " fue cancelado";
            default -> "Actualización de tu pedido #" + pedidoId;
        };
    }

    public String construirHtml(Long pedidoId, String estadoNuevo, String observacion) {
        String estado = normalizarEstado(estadoNuevo);
        String titulo = obtenerTitulo(estado);
        String mensaje = obtenerMensaje(estado);
        String observacionSegura = observacion == null || observacion.isBlank()
                ? "Sin observaciones adicionales."
                : observacion;

        return """
                <div style="font-family: Arial, sans-serif; background:#f4f6f8; padding:24px;">
                  <div style="max-width:620px; margin:0 auto; background:#ffffff; border-radius:14px; overflow:hidden; border:1px solid #e5e7eb;">
                    <div style="background:#172554; color:#ffffff; padding:20px 24px;">
                      <h1 style="margin:0; font-size:24px;">PymeTrack</h1>
                      <p style="margin:6px 0 0; color:#dbeafe;">Notificación de pedido</p>
                    </div>

                    <div style="padding:24px;">
                      <h2 style="margin:0 0 12px; color:#111827;">%s</h2>
                      <p style="font-size:15px; color:#374151; line-height:1.6;">
                        %s
                      </p>

                      <div style="background:#f9fafb; border:1px solid #e5e7eb; border-radius:10px; padding:16px; margin:20px 0;">
                        <p style="margin:0 0 8px;"><strong>Número de pedido:</strong> #%s</p>
                        <p style="margin:0 0 8px;"><strong>Estado actual:</strong> %s</p>
                        <p style="margin:0;"><strong>Observación:</strong> %s</p>
                      </div>

                      <p style="font-size:13px; color:#6b7280; line-height:1.5;">
                        Este correo fue generado automáticamente por PymeTrack. Por favor, no respondas directamente a este mensaje.
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(
                titulo,
                mensaje,
                pedidoId,
                estado,
                observacionSegura
        );
    }

    private String obtenerTitulo(String estado) {
        return switch (estado) {
            case "ASIGNADO" -> "Tu pedido fue asignado";
            case "EN_CAMINO" -> "Tu pedido está en camino";
            case "ENTREGADO" -> "Tu pedido fue entregado";
            case "RECHAZADO" -> "No fue posible continuar con tu pedido";
            case "CANCELADO" -> "Tu pedido fue cancelado";
            default -> "Tu pedido fue actualizado";
        };
    }

    private String obtenerMensaje(String estado) {
        return switch (estado) {
            case "ASIGNADO" -> "Tu pedido ya fue asignado a un repartidor. Pronto comenzará el proceso de despacho.";
            case "EN_CAMINO" -> "Tu pedido ya salió a despacho y va camino a la dirección registrada.";
            case "ENTREGADO" -> "Tu pedido fue entregado correctamente. Gracias por utilizar PymeTrack.";
            case "RECHAZADO" -> "El pedido no pudo ser aceptado o procesado por el repartidor asignado.";
            case "CANCELADO" -> "El pedido fue cancelado y no continuará su proceso de despacho.";
            default -> "El estado de tu pedido fue actualizado en nuestro sistema.";
        };
    }

    private String normalizarEstado(String estadoNuevo) {
        return estadoNuevo == null ? "DESCONOCIDO" : estadoNuevo.trim().toUpperCase();
    }
}