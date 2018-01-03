import com.mario.services.email.DefaultEmailEnvelope;
import com.mario.services.email.DefaultEmailService;
import com.mario.services.email.config.OutgoingMailServerConfig;
import com.mario.services.email.config.OutgoingMailServerConfig.EmailSecurityType;
import com.nhb.common.vo.UserNameAndPassword;

public class TestEmailSender {

	public static void main(String[] args) {
		OutgoingMailServerConfig mailConfig = new OutgoingMailServerConfig();
		mailConfig.setAuthenticator(
				new UserNameAndPassword(System.getProperty("testEmailUser"), System.getProperty("testEmailPassword")));
		
		mailConfig.setFrom("alert@puppetteam.com");
		mailConfig.setReplyTo("no-reply@puppetteam.com");
		mailConfig.setHost("smtp.gmail.com");
		mailConfig.setPort(465);
		mailConfig.setSecurityType(EmailSecurityType.SSL);

		DefaultEmailService emailService = new DefaultEmailService();
		emailService.setOutgoingConfig(mailConfig);
		emailService.setName("test");
		emailService.init(null);

		DefaultEmailEnvelope envelope = new DefaultEmailEnvelope();
		envelope.getTo().add("hoangbach.bk@gmail.com");
		envelope.getCc().add("bachnh@puppetteam.com");
		envelope.setContent("test");
		envelope.setSubject("this is subject");

		emailService.send(envelope);

		envelope = new DefaultEmailEnvelope();
		envelope.getTo().add("hoangbach.bk@gmail.com");
		envelope.setSubject("just a test");
		envelope.setContent("test----1");

		emailService.send(envelope);
	}
}
