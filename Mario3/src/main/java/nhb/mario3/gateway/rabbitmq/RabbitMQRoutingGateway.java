package nhb.mario3.gateway.rabbitmq;

import java.io.IOException;

public interface RabbitMQRoutingGateway {

	void queueBind(String rountingKey) throws IOException;

	void queueUnbind(String rountingKey) throws IOException;
}
