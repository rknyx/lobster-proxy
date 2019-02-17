package com.rk.conf;

import com.google.common.base.Objects;

public class PortMapping {
    public static final int PORT_MAX = 65535;
    public static final String LOCALHOST = "127.0.0.1";

    private Integer sourcePort;
    private String destHost;
    private Integer destPort;

    public PortMapping(Integer sourcePort, String destHost, Integer destPort) {
        this.sourcePort = sourcePort;
        this.destHost = destHost;
        this.destPort = destPort;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public String getDestHost() {
        return destHost;
    }

    public Integer getDestPort() {
        return destPort;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortMapping that = (PortMapping) o;
        return Objects.equal(sourcePort, that.sourcePort) &&
                Objects.equal(destHost, that.destHost) &&
                Objects.equal(destPort, that.destPort);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourcePort, destHost, destPort);
    }
}
