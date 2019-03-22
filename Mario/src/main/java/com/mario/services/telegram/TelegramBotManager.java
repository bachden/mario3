package com.mario.services.telegram;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import com.mario.api.MarioApiFactory;

public class TelegramBotManager {

	static {
		ApiContextInitializer.init();
	}

	private Map<String, TelegramBot> bots = new ConcurrentHashMap<>();

	private TelegramBotsApi botsApi = new TelegramBotsApi();

	private final List<TelegramLongPollingBot> longPollingBots = new CopyOnWriteArrayList<>();

	private final List<TelegramWebhookBot> webhookBots = new CopyOnWriteArrayList<>();

	public void register(TelegramBot bot) {
		if (bot != null) {
			if (bot.getType() != null) {
				switch (bot.getType()) {
				case LONG_POLLING:
					this.longPollingBots.add((TelegramLongPollingBot) bot);
					break;
				case WEBHOOK:
					this.webhookBots.add((TelegramWebhookBot) bot);
					break;
				}
			} else {
				throw new IllegalArgumentException("Bot's type must be not null");
			}
			this.bots.put(bot.getName(), bot);
		} else {
			throw new NullPointerException("Bot to be registered cannot be null");
		}
	}

	public void init(MarioApiFactory apiFactory) {
		for (TelegramBot bot : this.bots.values()) {
			bot.setApi(apiFactory.newApi());
			bot.init();

			if (bot.getRegisterStrategy() == TelegramBotRegisterStrategy.IMMEDIATELY) {
				this.registerWithApi(bot);
			}
		}
	}

	private void registerWithApi(TelegramBot bot) {
		try {
			if (bot.setRegistered()) {
				if (bot instanceof TelegramLongPollingBot) {
					this.botsApi.registerBot((TelegramLongPollingBot) bot);
				} else if (bot instanceof TelegramWebhookBot) {
					this.botsApi.registerBot((TelegramWebhookBot) bot);
				} else {
					throw new IllegalArgumentException("Invalid bot type");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Register bot error", e);
		}
	}

	public TelegramBot getBot(String name) {
		TelegramBot bot = this.bots.get(name);
		if (!bot.isRegistered()) {
			this.registerWithApi(bot);
		}
		return bot;
	}
}
