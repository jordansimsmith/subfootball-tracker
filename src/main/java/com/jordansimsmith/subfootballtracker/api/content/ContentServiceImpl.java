package com.jordansimsmith.subfootballtracker.api.content;

import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentScraper contentScraper;
    private final ContentChangeNotifier contentChangeNotifier;
    private final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    public ContentServiceImpl(
            ContentRepository contentRepository,
            ContentScraper contentScraper,
            ContentChangeNotifier contentChangeNotifier) {
        this.contentRepository = contentRepository;
        this.contentScraper = contentScraper;
        this.contentChangeNotifier = contentChangeNotifier;
    }

    @Override
    public Content checkForUpdates() {
        // get current content
        var content = contentScraper.scrapeRegistration();
        logger.info("Scraped content from external source");

        // check for historical content
        var historicalContent =
                contentRepository
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
        if (historicalContent.isEmpty() || !content.equals(historicalContent.get().getContent())) {
            logger.info("Content has changed");
            try {
                contentChangeNotifier.notify(newContent);
                logger.info("Content change notification has been dispatched");
            } catch (IOException e) {
                logger.error("Failed to dispatch content change email");
            }
        }

        return newContent;
    }
}
