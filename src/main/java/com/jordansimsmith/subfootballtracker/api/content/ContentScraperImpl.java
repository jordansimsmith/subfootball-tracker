package com.jordansimsmith.subfootballtracker.api.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ContentScraperImpl implements ContentScraper {

    private static final String REGISTRATION_URI = "https://subfootball.com/register";

    private final Logger logger = LoggerFactory.getLogger(ContentScraperImpl.class);

    @Override
    public String scrapeRegistration() {
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
        return content.text().replaceAll("\s*\\\\n\s*", "\n").trim();
    }
}
