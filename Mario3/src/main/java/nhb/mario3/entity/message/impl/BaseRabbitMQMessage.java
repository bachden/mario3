package nhb.mario3.entity.message.impl;

import nhb.mario3.entity.message.CloneableMessage;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.RabbitMQDeliveredMessage;
import nhb.mario3.entity.message.RabbitMQMessage;

public class BaseRabbitMQMessage extends BaseMessage implements RabbitMQMessage, CloneableMessage {

	private RabbitMQDeliveredMessage deliveredMessage;

	@Override
	public void clear() {
		super.clear();
		this.deliveredMessage = null;
	}

	@Override
	public RabbitMQDeliveredMessage getDeliveredMessage() {
		return deliveredMessage;
	}

	@Override
	public void setDeliveredMessage(RabbitMQDeliveredMessage deliveredMessage) {
		this.deliveredMessage = deliveredMessage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Message> T makeClone() {
		BaseRabbitMQMessage result = new BaseRabbitMQMessage();
		this.fillProperties(result);
		result.deliveredMessage = this.deliveredMessage;
		return (T) result;
	}
}
