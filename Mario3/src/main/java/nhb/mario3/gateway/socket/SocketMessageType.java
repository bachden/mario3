package nhb.mario3.gateway.socket;

public enum SocketMessageType {

	OPENED, CLOSED, MESSAGE;

	public static final SocketMessageType fromName(String name) {
		if (name != null) {
			for (SocketMessageType type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
