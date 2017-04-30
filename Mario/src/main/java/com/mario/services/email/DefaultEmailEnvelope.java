package com.mario.services.email;

import java.util.HashSet;
import java.util.Set;

import javax.mail.Multipart;

import lombok.Setter;

import lombok.Getter;

@Setter
@Getter
public class DefaultEmailEnvelope implements EmailEnvelope {

	private final Set<String> to = new HashSet<>();

	private final Set<String> cc = new HashSet<>();

	private final Set<String> bcc = new HashSet<>();

	private String subject;

	private String content;

	private Multipart multipart;

}
