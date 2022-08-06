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

        // check for historical content
        var historicalContent = contentRepository
                .findAll(PageRequest.of(0, 1, Sort.by(Sort.Order.desc("date"))))
                .get()
                .findFirst();

        // insert content record
        if (historicalContent.isEmpty()) {
            var newContent = new Content();
            newContent.setContent(content);
            newContent.setDate(new Date());
            contentRepository.save(newContent);
            return;
        }

        // dispatch notification if changed
        // TODO:
        if (!content.equals(historicalContent.get().getContent())) {
            logger.info("Content has changed");
        }
    }
}
