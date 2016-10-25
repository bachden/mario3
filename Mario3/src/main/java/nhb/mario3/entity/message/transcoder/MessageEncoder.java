package nhb.mario3.entity.message.transcoder;

public interface MessageEncoder {

	Object encode(Object data) throws Exception;
}
