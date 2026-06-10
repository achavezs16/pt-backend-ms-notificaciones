package cl.pymetrack.msnotificaciones.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PedidoEstadoConsumer {

    @RabbitListener(queues = "pedido.estado.actualizado")
    public void consumirCambioEstado(Map<String, Object> event) {
        System.out.println("======================================");
        System.out.println("📩 EVENTO RECIBIDO DESDE RABBITMQ");
        System.out.println("Pedido ID: " + event.get("pedidoId"));
        System.out.println("PYME ID: " + event.get("idPyme"));
        System.out.println("Estado anterior: " + event.get("estadoAnterior"));
        System.out.println("Estado nuevo: " + event.get("estadoNuevo"));
        System.out.println("Repartidor ID: " + event.get("repartidorId"));
        System.out.println("Observación: " + event.get("observacion"));
        System.out.println("Fecha evento: " + event.get("fechaEvento"));
        System.out.println("📧 Simulando envío de correo al cliente...");
        System.out.println("✅ Correo enviado correctamente");
        System.out.println("======================================");
    }
}