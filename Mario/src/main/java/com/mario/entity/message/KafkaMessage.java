package com.mario.entity.message;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.nhb.common.data.PuElement;

public interface KafkaMessage extends Message {

	public void setKey(byte[] key);

	public byte[] getKey();

	public String getTopic();

	public void setTopic(String topic);

	public List<ConsumerRecord<byte[], PuElement>> getRecords();

	public void setRecords(List<ConsumerRecord<byte[], PuElement>> records);

	/**
	 * In case gateway use minBatchingSize <= 0 (auto split batch into single
	 * pieces), this method return partition id which record has been polled
	 * 
	 * @return
	 */
	public int getPartition();

	/**
	 * @see {@link #KafkaMessage.getPartition()}
	 * @param partition
	 */
	public void setPartition(int partition);
}
