package com.jordansimsmith.subfootballtracker.api.content;

public interface ContentService {

    /**
     * Checks whether the external has been updated, and dispatches a notificiation if it has.
     */
    void checkForUpdates();
}
