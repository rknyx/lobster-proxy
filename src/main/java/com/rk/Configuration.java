package com.rk;

import java.util.List;
import java.util.Map;

public class Configuration {
    private Map<Endpoint, List<Endpoint>> map;
    boolean logContentToStdOut;

    public Configuration(Map<Endpoint, List<Endpoint>>  map, boolean logContentToStdOut) {
        this.map = map;
        this.logContentToStdOut = logContentToStdOut;
    }

    public Map<Endpoint, List<Endpoint>> getMap() {
        return map;
    }

    public boolean isLogContentToStdOut() {
        return logContentToStdOut;
    }
}
