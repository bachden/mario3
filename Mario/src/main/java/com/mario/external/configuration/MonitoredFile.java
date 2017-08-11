package com.mario.external.configuration;

import java.io.File;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MonitoredFile {

	public static final class Sensitivity {
		public static final String HIGH = "HIGH";
		public static final String LOW = "LOW";
		public static final String MEDIUM = "MEDIUM";
	}

	private File file;
	private String sensitivity;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MonitoredFile) {
			return this.getFile().getAbsolutePath().equals(((MonitoredFile) obj).getFile().getAbsolutePath()) //
					&& ((this.getSensitivity() == null && ((MonitoredFile) obj).getSensitivity() == null)
							|| (this.getSensitivity() != null
									&& this.getSensitivity().equals(((MonitoredFile) obj).getSensitivity())));
		}
		return false;
	}

}
