package com.mario.services.email;

import java.util.Set;

import javax.mail.Multipart;

public interface EmailEnvelope {

	Set<String> getTo();

	Set<String> getCc();

	Set<String> getBcc();

	String getSubject();

	String getContent();

	Multipart getMultipart();
}
