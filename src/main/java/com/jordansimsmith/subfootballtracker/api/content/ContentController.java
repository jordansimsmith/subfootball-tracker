package com.jordansimsmith.subfootballtracker.api.content;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("content")
public class ContentController {

    private final ContentScraper contentScraper;

    public ContentController(ContentScraper contentScraper) {
        this.contentScraper = contentScraper;
    }

    @GetMapping("")
    public String index() {
        return this.contentScraper.scrapeRegistration();
    }
}
