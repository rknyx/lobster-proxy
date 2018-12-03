package com.rk;

import com.rk.domain.Endpoints;

import java.util.Set;

public class Configuration {
    private Set<Endpoints> endpoints;
    boolean logContent;

    public Configuration(Set<Endpoints> map, boolean logContentToStdOut) {
        this.endpoints = map;
        this.logContent = logContentToStdOut;
    }

    public Set<Endpoints> getEndpoints() {
        return endpoints;
    }

    public boolean isLogContent() {
        return logContent;
    }
}
