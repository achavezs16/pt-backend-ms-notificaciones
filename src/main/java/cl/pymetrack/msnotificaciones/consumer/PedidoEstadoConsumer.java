package cl.pymetrack.msnotificaciones.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoEstadoConsumer {

    @RabbitListener(queues = "pedido.estado.actualizado")
    public void consumirCambioEstado(Message message) {
        String exchange = message.getMessageProperties().getReceivedExchange();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String contentType = message.getMessageProperties().getContentType();

        System.out.println("======================================");
        System.out.println("📩 EVENTO RECIBIDO DESDE RABBITMQ");
        System.out.println("Exchange: " + exchange);
        System.out.println("Routing key: " + routingKey);
        System.out.println("Content type: " + contentType);
        System.out.println("📧 Simulando envío de correo al cliente...");
        System.out.println("✅ Correo enviado correctamente");
        System.out.println("======================================");
    }
}