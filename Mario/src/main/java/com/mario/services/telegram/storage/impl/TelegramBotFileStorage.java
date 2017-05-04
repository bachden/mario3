package com.mario.services.telegram.storage.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mario.services.telegram.storage.TelegramBotStorage;
import com.nhb.common.Loggable;

public class TelegramBotFileStorage implements TelegramBotStorage, Loggable {

	private final File file;

	private String botUsername;

	private ExecutorService writingExecutor = Executors.newFixedThreadPool(1);

	private final Map<String, Long> userIdToChatId = new ConcurrentHashMap<>();

	public TelegramBotFileStorage(String filePath, String botUsername) {
		this.botUsername = botUsername;

		this.file = new File(filePath);
		if (!this.file.exists()) {
			if (!this.file.getParentFile().exists()) {
				this.file.getParentFile().mkdirs();
			}
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Cannot create new file at: " + filePath);
			}
		} else if (this.file.isDirectory()) {
			throw new RuntimeException("Invalid file path, existing folder detected at: " + filePath);
		} else {
			getLogger().warn("File at {} is existing...", filePath);
		}
		this.loadFile();
	}

	private void loadFile() {
		try {
			List<String> content = Files.readAllLines(this.file.toPath());
			for (String line : content) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] arr = line.split(":");
				if (arr.length == 3) {
					if (arr[0].trim().equals(this.botUsername)) {
						this.userIdToChatId.put(arr[1], Long.valueOf(arr[2]));
					}
				} else {
					getLogger().warn("Invalid line: " + line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file at " + this.file.getPath());
		}

	}

	@Override
	public boolean saveChatId(String phoneNumber, long chatId) {
		if (phoneNumber == null || chatId <= 0) {
			throw new IllegalArgumentException("User id and chat id must be not-null and > 0");
		}
		Long oldValue = this.userIdToChatId.putIfAbsent(phoneNumber, chatId);
		if (oldValue == null) {
			return _write(this.botUsername + ":" + phoneNumber + ":" + chatId);
		}
		return false;
	}

	private boolean _write(final String line) {
		final AtomicBoolean success = new AtomicBoolean(false);
		try {
			this.writingExecutor.submit(new Runnable() {

				@Override
				public void run() {
					try (BufferedWriter output = new BufferedWriter(new FileWriter(file, true))) {
						output.append(line);
						success.set(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Cannot wait for writing process to be completed", e);
		}
		return success.get();
	}

	@Override
	public long getChatId(String phoneNumber) {
		Long value = this.userIdToChatId.get(phoneNumber);
		return value == null ? -1 : value.longValue();
	}
}
