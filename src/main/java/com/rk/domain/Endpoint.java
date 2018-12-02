package com.rk.domain;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Endpoint {
    private static final String LOCALHOST = "127.0.0.1";
    private static final String INVALID_PORT_PATTERN = "Port should be less than '%s' and more than '%s', actual: '%s'";

    private static final int PORT_MIN = 0;
    private static final int PORT_MAX = 65535;
    private String host;
    private int port;

    public Endpoint(String host, int port) {
        java.util.Objects.requireNonNull(host);
        java.util.Objects.requireNonNull(port);
        if (port < PORT_MIN || port > PORT_MAX) {
            throw new IllegalArgumentException(String.format(INVALID_PORT_PATTERN, PORT_MIN, PORT_MAX, port));
        }
        this.host = host;
        this.port = port;
    }

    public static Endpoint from(String host, int port) {
        return new Endpoint(host, port);
    }

    public static Endpoint fromLocalhostPort(int port) {
        return from(LOCALHOST, port);
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
