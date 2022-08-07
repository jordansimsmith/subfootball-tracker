package com.jordansimsmith.subfootballtracker.api.content;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailContentChangeNotifier implements ContentChangeNotifier {

    private final Logger logger = LoggerFactory.getLogger(EmailContentChangeNotifier.class);

    private final EmailProperties emailProperties;

    @Autowired
    public EmailContentChangeNotifier(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }

    @Override
    public void notify(Content content) throws IOException {
        var mail = new Mail();
        mail.setSubject("SUB Football registration content changes");

        var fromEmail = new Email();
        fromEmail.setName(emailProperties.getSenderName());
        fromEmail.setEmail(emailProperties.getSenderAddress());
        mail.setFrom(fromEmail);

        // TODO: pull from subscribers in config
        for (var subscriber : emailProperties.getSubscribers()) {
            var toEmail = new Email();
            toEmail.setName(subscriber.getName());
            toEmail.setEmail(subscriber.getEmail());

            var personalisation = new Personalization();
            personalisation.addTo(toEmail);
            personalisation.addDynamicTemplateData("content", content.getContent().split("\\n"));
            personalisation.addDynamicTemplateData("name", subscriber.getName());
            mail.addPersonalization(personalisation);
        }

        mail.setTemplateId(emailProperties.getContentChangeTemplateId());

        var client = new SendGrid(emailProperties.getSendGridApiKey());

        var request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        var response = client.api(request);
        var status = HttpStatus.valueOf(response.getStatusCode());
        if (!status.is2xxSuccessful()) {
            logger.error("Failed to dispatch content change email with status code " + response.getStatusCode());
        }
    }
}
