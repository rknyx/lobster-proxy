package com.rk;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Endpoint {
    private String host;
    private int port;

    public Endpoint(String host, int port) {
        java.util.Objects.requireNonNull(host);
        java.util.Objects.requireNonNull(port);
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;

        return new EqualsBuilder()
                .append(port, endpoint.port)
                .append(host, endpoint.host)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(host)
                .append(port)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", host, port);
    }
}
