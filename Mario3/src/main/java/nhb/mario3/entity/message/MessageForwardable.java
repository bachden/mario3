package nhb.mario3.entity.message;

import nhb.messaging.MessageForwarder;
import nhb.messaging.MessageProducer;

public interface MessageForwardable extends MessageRW {

	void setForwarder(MessageForwarder forwarder);

	MessageForwarder getForwarder();

	default <E, T extends MessageProducer<E>> E forward(T producer) {
		if (this.getForwarder() != null) {
			return (E) this.getForwarder().forward(producer);
		}
		return null;
	}

	default <E, T extends MessageProducer<E>> E forward(T producer, String routingKey) {
		if (this.getForwarder() != null) {
			return (E) this.getForwarder().forward(producer, routingKey);
		}
		return null;
	}
}
