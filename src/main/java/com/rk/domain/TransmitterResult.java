package com.rk.domain;

public class TransmitterResult {
    private final long bytesTransmitted;

    public TransmitterResult(long bytesTransmitted) {
        this.bytesTransmitted = bytesTransmitted;
    }

    public long getBytesTransmitted() {
        return bytesTransmitted;
    }
}
