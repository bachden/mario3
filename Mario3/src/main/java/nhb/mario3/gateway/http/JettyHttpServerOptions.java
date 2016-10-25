package nhb.mario3.gateway.http;

public enum JettyHttpServerOptions {
	NO_SESSIONS(0), NO_SECURITY(0), SESSIONS(1), SECURITY(2), GZIP(4);

	private int code;

	private JettyHttpServerOptions(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

	public static final JettyHttpServerOptions fromCode(int code) {
		for (JettyHttpServerOptions option : values()) {
			if (option.getCode() == code) {
				return option;
			}
		}
		return null;
	}

	public static final JettyHttpServerOptions fromName(String name) {
		for (JettyHttpServerOptions option : values()) {
			if (option.name().toLowerCase().equalsIgnoreCase(name)) {
				return option;
			}
		}
		return null;
	}
}