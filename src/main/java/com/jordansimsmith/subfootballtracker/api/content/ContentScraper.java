package com.jordansimsmith.subfootballtracker.api.content;

public interface ContentScraper {
    /**
     * Scrapes registration content and returns a plain text representation
     * @return registration content or null if an error occurred
     */
    String scrapeRegistration();
}
