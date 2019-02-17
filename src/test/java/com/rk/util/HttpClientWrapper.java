package com.rk.util;

import com.rk.conf.PortMapping;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpClientWrapper implements Closeable {
    private PortMapping portMapping;
    private HttpClient client;
    private Map<String, String> requestResponseMap = new HashMap<>();

    public HttpClientWrapper(PortMapping portMapping) {
        Objects.requireNonNull(portMapping);
        this.portMapping = portMapping;
    }

    public void send() {
        try {
            if (client == null) {
                client = new HttpClient();
                client.start();
            }
            final String request = RandomStringUtils.randomAlphabetic(16);
            final byte[] content = client.newRequest(PortMapping.LOCALHOST, portMapping.getSourcePort())
                    .content(new StringContentProvider(request))
                    .send()
                    .getContent();
            final String response = new String(content, Charset.defaultCharset());
            requestResponseMap.put(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getRequestResponseMap() {
        return requestResponseMap;
    }

    @Override
    public void close() {
        try {
            client.stop();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
