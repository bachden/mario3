package nhb.mario3.worker;

import com.lmax.disruptor.EventFactory;

import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.impl.BaseMessage;

public interface MessageEventFactory extends EventFactory<Message> {

	@Override
	default Message newInstance() {
		BaseMessage result = new BaseMessage();
		return result;
	}

}
