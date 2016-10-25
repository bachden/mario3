package nhb.mario3.entity;

import nhb.common.data.PuElement;
import nhb.mario3.entity.message.Message;

public interface MessageHandleCallback {

	void onHandleComplete(Message message, PuElement result);

	void onHandleError(Message message, Throwable exception);
}
