package com.mario.services.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.nhb.common.data.PuObjectRO;

import lombok.AccessLevel;
import lombok.Getter;

public class DefaultEmailService extends AbstractEmailService {

	@Getter(AccessLevel.PROTECTED)
	private Session outgoingSession;

	private void initOutgoing() {
		if (this.getOutgoingConfig() != null) {
			Properties mailProps = initMailProps();

			Session session = null;
			if (this.getOutgoingConfig().getAuthenticator() == null) {
				// Get the default Session object.
				session = Session.getDefaultInstance(mailProps);
			} else {
				Authenticator authenticator = new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(getOutgoingConfig().getAuthenticator().getUserName(),
								getOutgoingConfig().getAuthenticator().getPassword());
					}
				};
				session = Session.getDefaultInstance(mailProps, authenticator);
			}

			this.outgoingSession = session;
		}
	}

	private void initIncoming() {
		if (this.getIncomingConfig() != null) {
			getLogger().info("INCOMING MAIL SERVER IS NOT SUPPORTED RIGHT NOW, THE CONFIG WILL BE IGNORED...");
		}
	}

	private Properties initMailProps() {
		Properties props = new Properties();
		props.put("mail.smtp.host", this.getOutgoingConfig().getHost());
		props.put("mail.smtp.port", String.valueOf(this.getOutgoingConfig().getPort()));
		if (this.getOutgoingConfig().getAuthenticator() != null) {
			props.put("mail.smtp.auth", "true");
		}

		switch (this.getOutgoingConfig().getSecurityType()) {
		case NONE:
			// do nothing...
			break;
		case SSL:
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			break;
		case TLS:
			props.put("mail.smtp.starttls.enable", "true");
			break;
		}

		return props;
	}

	@Override
	public void send(EmailEnvelope envelope) {
		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(this.outgoingSession);

			if (this.getOutgoingConfig().getFrom() != null) {
				// Set From: header field of the header.
				message.setFrom(new InternetAddress(this.getOutgoingConfig().getFrom()));
			}

			if (this.getOutgoingConfig().getReplyTo() != null) {
				// Set replyTo: header field of the header.
				message.setReplyTo(InternetAddress.parse(this.getOutgoingConfig().getReplyTo(), false));
			}

			for (String to : envelope.getTo()) {
				// Set To: header field of the header.
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}

			if (envelope.getCc() != null) {
				for (String cc : envelope.getCc()) {
					// Set cc: header field of the header.
					message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
				}
			}

			if (envelope.getBcc() != null) {
				for (String bcc : envelope.getBcc()) {
					// Set bcc: header field of the header.
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
				}
			}

			// Set Subject: header field
			message.setSubject(envelope.getSubject(), "UTF-8");

			// Now set the actual message
			message.setText(envelope.getContent(), "UTF-8");

			// Send message
			Transport.send(message);
			getLogger().info("Sent message successfully....");
		} catch (Exception e) {
			throw new RuntimeException("Email sending error", e);
		}
	}

	@Override
	public void init(PuObjectRO initParams) {
		this.initIncoming();
		this.initOutgoing();
	}

	@Override
	public void destroy() throws Exception {
		// do nothing
	}

}
