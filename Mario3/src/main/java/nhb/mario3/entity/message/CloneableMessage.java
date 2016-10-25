package nhb.mario3.entity.message;

public interface CloneableMessage {

	<T extends Message> T makeClone();
}
