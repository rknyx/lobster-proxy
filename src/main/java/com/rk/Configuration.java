package com.rk;

import com.rk.domain.Endpoints;

import java.util.Set;

public class Configuration {
    private Set<Endpoints> endpoints;
    boolean logContentToStdOut;

    public Configuration(Set<Endpoints> map, boolean logContentToStdOut) {
        this.endpoints = map;
        this.logContentToStdOut = logContentToStdOut;
    }

    public Set<Endpoints> getEndpoints() {
        return endpoints;
    }

    public boolean isLogContentToStdOut() {
        return logContentToStdOut;
    }
}
