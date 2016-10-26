package com.mario.entity.impl;

import java.util.Map.Entry;

import com.mario.entity.message.Message;
import com.mario.statics.Fields;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.strategy.CommandController;
import com.nhb.strategy.CommandProcessor;
import com.nhb.strategy.CommandResponseData;

public abstract class BaseCommandRoutingHandler extends BaseMessageHandler {
	private CommandController commandController;

	public CommandController getCommandController() {
		return commandController;
	}

	protected void setCommandController(CommandController commandController) {
		this.commandController = commandController;
		if (this.commandController != null) {
			this.commandController.setEnviroiment(Fields.HANDLER, this);
		}
	}

	protected void initCommandController(PuObject defination) throws Exception {
		if (defination != null) {
			if (this.getCommandController() == null) {
				this.setCommandController(new CommandController());
			}
			for (Entry<String, PuValue> entry : defination) {
				if (entry.getValue() != null && entry.getValue().getType() == PuDataType.STRING) {
					this.commandController.registerCommand(entry.getKey(), (CommandProcessor) this.getClass()
							.getClassLoader().loadClass((String) entry.getValue().getData()).newInstance());
				}
			}
		}
	}

	protected CommandResponseData processCommand(Message message) {
		String type = null;
		if (message != null && message.getData() instanceof PuObject) {
			type = ((PuObject) message.getData()).getString(Fields.COMMAND, null);
		}
		if (type != null) {
			try {
				return this.commandController.processCommand(type, message);
			} catch (Exception e) {
				throw new ProcessCommandExcepion(e);
			}
		}
		throw new RuntimeException("Command cannot be null");
	}

	protected CommandResponseData processCommand(String type, Message message) {
		if (type != null) {
			try {
				return this.commandController.processCommand(type, message);
			} catch (Exception e) {
				throw new ProcessCommandExcepion(e);
			}
		}
		throw new RuntimeException("Command cannot be null");
	}
}
