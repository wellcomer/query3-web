package com.github.wellcomer.query3.servlet;

import com.github.wellcomer.query3.core.QueryList;
import com.github.wellcomer.query3.core.QueryStorage;

/**
 * Единственный экземпляр класса QueryList для общего использования сервлетами.
 */
public class QueryListSingle {

    private static QueryList queryListInstance = null;

    public static QueryList getInstance (String stgBackendName, String dbPath, String dbName, String characterEncoding){

        if (queryListInstance == null) {
            QueryStorage stgBackendInstance = com.github.wellcomer.query3.core.Main.getStgBackendInstance(stgBackendName, dbPath, dbName, characterEncoding);
            queryListInstance = new QueryList(stgBackendInstance);
        }
        return queryListInstance;
    }
}
