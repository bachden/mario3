package nhb.mario3.entity.message;

public interface RabbitMQMessage extends MessageForwardable {

	void setDeliveredMessage(RabbitMQDeliveredMessage deliveredMessage);

	RabbitMQDeliveredMessage getDeliveredMessage();
}
