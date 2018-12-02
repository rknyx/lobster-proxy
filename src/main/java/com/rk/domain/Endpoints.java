package com.rk.domain;

import com.google.common.base.Objects;

public class Endpoints {
    private final Endpoint src;
    private final Endpoint dst;

    private Endpoints(Endpoint src, Endpoint dst) {
        this.src = src;
        this.dst = dst;
    }

    public static Endpoints from(Endpoint src, Endpoint dst) {
        return new Endpoints(src, dst);
    }

    public static Endpoints from(String srcHost, int srcPort, String dstHost, int dstPort) {
        return Endpoints.from(Endpoint.from(srcHost, srcPort), Endpoint.from(dstHost, dstPort));
    }

    public Endpoint getSrc() {
        return src;
    }

    public Endpoint getDst() {
        return dst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoints that = (Endpoints) o;
        return Objects.equal(src, that.src) &&
                Objects.equal(dst, that.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(src, dst);
    }
}
