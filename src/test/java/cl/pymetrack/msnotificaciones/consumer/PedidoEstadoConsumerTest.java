package cl.pymetrack.msnotificaciones.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class PedidoEstadoConsumerTest {

    @InjectMocks
    private PedidoEstadoConsumer pedidoEstadoConsumer;

    @Test
    void consumirCambioEstado_Success() {
        // 1. Preparamos un mapa simulado idéntico al que enviaría RabbitMQ
        Map<String, Object> eventoMock = new HashMap<>();
        eventoMock.put("pedidoId", 123L);
        eventoMock.put("idPyme", 45L);
        eventoMock.put("estadoAnterior", "PENDIENTE");
        eventoMock.put("estadoNuevo", "EN_PREPARACION");
        eventoMock.put("repartidorId", 99L);
        eventoMock.put("observacion", "Pedido frágil");
        eventoMock.put("fechaEvento", "2026-06-19T20:50:00");

        // 2. Ejecutamos el método y verificamos que pase limpiamente sin lanzar errores
        assertDoesNotThrow(() -> pedidoEstadoConsumer.consumirCambioEstado(eventoMock));
    }
}