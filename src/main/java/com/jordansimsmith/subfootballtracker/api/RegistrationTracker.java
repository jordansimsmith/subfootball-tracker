package com.jordansimsmith.subfootballtracker.api;

import static com.jordansimsmith.subfootball.tracker.jooq.tables.Content.CONTENT;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.zookeeper.KeeperException;
import org.jooq.DSLContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RegistrationTracker extends LeaderSelectorListenerAdapter {
    private static final String LAST_RUN_PATH = "/registration-tracker/last-run-unix";
    private static final long RUN_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(6);
    private static final String REGISTRATION_URI = "https://subfootball.com/register";

    private final Logger logger = LoggerFactory.getLogger(RegistrationTracker.class);

    private final DSLContext ctx;
    private final EmailProperties emailProperties;

    @Autowired
    public RegistrationTracker(DSLContext ctx, EmailProperties emailProperties) {
        this.ctx = ctx;
        this.emailProperties = emailProperties;
    }

    @Override
    public void takeLeadership(CuratorFramework curator) throws Exception {
        try {
            doTakeLeadership(curator);
        } catch (Exception e) {
            logger.error("Failed to track the registration page", e);
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            throw e;
        }
    }

    public void doTakeLeadership(CuratorFramework curator) throws Exception {
        logger.info("RegistrationTracker running...");

        // check if it is time to run yet
        var now = Instant.now().getEpochSecond();
        var lastRun = getPathValue(curator, LAST_RUN_PATH);
        if (lastRun + RUN_INTERVAL_SECONDS > now) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            return;
        }

        var contents = scrapeRegistrationContents();
        logger.info("Scraped registration content from external source");

        var previousContents =
                ctx.selectFrom(CONTENT).orderBy(CONTENT.DATE.desc()).limit(1).fetchOne();
        logger.info("Loaded previous registration contnet");

        if (previousContents == null || !Objects.equals(contents, previousContents.getContent())) {
            logger.info("Registration content has changed");
            notifySubscribersOfContentChange(contents);
            logger.info("Registration content change notification has been dispatched");
        }

        // insert the new values
        var newContents = ctx.dsl().newRecord(CONTENT);
        newContents.setDate(LocalDateTime.ofEpochSecond(now, 0, ZoneOffset.UTC));
        newContents.setContent(contents);
        newContents.insert();
        logger.info("Registration content change notification has been dispatched");

        setPathValue(curator, LAST_RUN_PATH, now);
    }

    private String scrapeRegistrationContents() {
        // load document from the external uri
        Document doc;
        try {
            doc = Jsoup.connect(REGISTRATION_URI).get();
        } catch (IOException e) {
            this.logger.error("Error requesting registration content", e);
            return null;
        }

        // locate content on the document
        var content = doc.selectFirst(".page.content-item");
        if (content == null) {
            this.logger.error("No registration content was found on the page");
            return null;
        }

        // preserve line natural breaks
        content.select("br").before("\\n");
        content.select("p").before("\\n");
        return content.text().replaceAll(" *\\\\n *", "\n").trim();
    }

    public void notifySubscribersOfContentChange(String contents) throws IOException {
        var mail = new Mail();
        mail.setSubject("SUB Football registration content changes");

        var fromEmail = new Email();
        fromEmail.setName(emailProperties.senderName());
        fromEmail.setEmail(emailProperties.senderAddress());
        mail.setFrom(fromEmail);

        for (var subscriber : emailProperties.subscribers()) {
            var toEmail = new Email();
            toEmail.setName(subscriber.name());
            toEmail.setEmail(subscriber.email());

            var personalisation = new Personalization();
            personalisation.addTo(toEmail);
            personalisation.addDynamicTemplateData("content", contents.split("\\n"));
            personalisation.addDynamicTemplateData("name", subscriber.name());
            mail.addPersonalization(personalisation);
        }

        mail.setTemplateId(emailProperties.contentChangeTemplateId());

        var client = new SendGrid(emailProperties.sendGridApiKey());

        var request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        var response = client.api(request);
        var status = HttpStatus.valueOf(response.getStatusCode());
        if (!status.is2xxSuccessful()) {
            logger.error(
                    "Failed to dispatch content change email with status code "
                            + response.getStatusCode());
        }
        throw new IOException("Failed to dispatch email");
    }

    private void setPathValue(CuratorFramework curator, String path, long value) throws Exception {
        curator.setData().forPath(path, String.valueOf(value).getBytes());
    }

    private long getPathValue(CuratorFramework curator, String path) throws Exception {
        try {
            var data = curator.getData().forPath(path);
            if (data != null && data.length != 0) {
                return Long.parseLong(new String(data));
            }
        } catch (KeeperException.NoNodeException e) {
            // Allowable, continue with null data, but create so can be used to store later.
            curator.create().creatingParentsIfNeeded().forPath(path, String.valueOf(0).getBytes());
        }

        return 0;
    }
}
