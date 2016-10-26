package com.mario.entity.message;

public interface KafkaMessage extends Message {

	public void setKey(byte[] key);

	public byte[] getKey();

	public String getTopic();

	public void setTopic(String topic);
}
