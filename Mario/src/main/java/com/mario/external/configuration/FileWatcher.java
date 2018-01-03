package com.mario.external.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.nhb.common.Loggable;
import com.nhb.common.async.Callback;
import com.nhb.common.utils.Initializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileWatcher implements Loggable {

	private Thread watchThread;
	private WatchService watchService;

	static {
		Initializer.bootstrap(FileWatcher.class);
	}

	public static void main(String[] args) throws InterruptedException {
		new FileWatcher(Arrays.asList(MonitoredFile.builder().file(new File("temp/test.txt"))
				.sensitivity(MonitoredFile.Sensitivity.HIGH).build()), new Callback<File>() {

					@Override
					public void apply(File file) {
						log.debug("file changed: {}", file.getAbsolutePath());
					}
				}).start();

		Thread.sleep((long) 1e9);
	}

	FileWatcher(Collection<MonitoredFile> monitoredFiles, Callback<File> onChange) {
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create watch service", e);
		}

		Map<WatchKey, Path> watchKeyToMonitoredPaths = new HashMap<>();

		for (MonitoredFile monitoredFile : monitoredFiles) {
			File file = monitoredFile.getFile();
			File folder = file.getParentFile();
			Path path = folder.getAbsoluteFile().toPath();

			try {
				final WatchKey watchKey;
				if (monitoredFile.getSensitivity() != null) {
					watchKey = path.register(watchService,
							new Kind[] { StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE,
									StandardWatchEventKinds.ENTRY_DELETE },
							getSensitivityWatchEventModifier(monitoredFile.getSensitivity()));
				} else {
					watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
							StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
				}
				watchKeyToMonitoredPaths.put(watchKey, path);
			} catch (Exception e) {
				throw new RuntimeException("An error occurs while register watch service to path: " + path, e);
			}
		}

		this.watchThread = new Thread("ExternalConfiguration file watcher") {
			@Override
			public void run() {
				while (true) {
					try {
						WatchKey watchKey = watchService.take();
						for (WatchEvent<?> event : watchKey.pollEvents()) {
							Path changed = (Path) event.context();
							File changedFile = watchKeyToMonitoredPaths.get(watchKey).resolve(changed).toFile();
							if (onChange != null) {
								onChange.apply(changedFile);
							}
						}
						// reset the key
						if (!watchKey.reset()) {
							getLogger().warn("Key has been unregistered");
						}
					} catch (InterruptedException e) {
						getLogger().debug("Cannot continue watching file", e);
						break;
					}
				}
			}
		};
	}

	private static Modifier getSensitivityWatchEventModifier(String name) {
		try {
			Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
			return (Modifier) c.getField(name).get(c);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get com.sun.nio.file.SensitivityWatchEventModifier", e);
		}
	}

	public void start() {
		this.watchThread.start();
	}

	public void stop() {
		if (this.watchThread != null && this.watchThread.isAlive()) {
			this.watchThread.interrupt();
		}
		try {
			this.watchService.close();
		} catch (IOException e) {
			getLogger().error("Error while closing watch service", e);
		}
	}
}
