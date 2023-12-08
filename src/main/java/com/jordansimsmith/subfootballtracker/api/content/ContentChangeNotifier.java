package com.jordansimsmith.subfootballtracker.api.content;

import java.io.IOException;

public interface ContentChangeNotifier {
    /**
     * Notifies subscribers that the content has changed
     *
     * @param content the latest content version
     */
    void notify(Content content) throws IOException;
}
