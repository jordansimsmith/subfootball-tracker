package com.jordansimsmith.subfootballtracker.api.content;

public interface ContentService {

    /** Checks whether the external has been updated, and dispatches a notification if it has. */
    Content checkForUpdates();
}
