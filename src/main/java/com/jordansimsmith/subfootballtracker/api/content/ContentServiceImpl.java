package com.jordansimsmith.subfootballtracker.api.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentScraper contentScraper;
    private final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    public ContentServiceImpl(ContentRepository contentRepository, ContentScraper contentScraper) {
        this.contentRepository = contentRepository;
        this.contentScraper = contentScraper;
    }

    @Override
    public void checkForUpdates() {
        // get current content
        var content = contentScraper.scrapeRegistration();
        logger.info("Scraped content from external source");

        // check for historical content
        var historicalContent = contentRepository
                .findAll(PageRequest.of(0, 1, Sort.by(Sort.Order.desc("date"))))
                .get()
                .findFirst();
        logger.info("Loaded latest historical content");

        // insert content record
        var newContent = new Content();
        newContent.setContent(content);
        newContent.setDate(new Date());
        contentRepository.save(newContent);
        logger.info("Saved new content history");

        // dispatch notification if changed
        // TODO:
        if (historicalContent.isPresent() && !content.equals(historicalContent.get().getContent())) {
            logger.info("Content has changed");
        }
    }
}
