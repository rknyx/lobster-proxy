package com.rk.util;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpServerWrapper implements Closeable {
    private int port;
    private Server server;

    private Map<String, String> responseRequestMap = new HashMap<>();


    public HttpServerWrapper(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        if (server != null) {
            throw new IllegalStateException("Cannot start server second time. Call stop before.");
        }

        server = new Server(port);
        server.setHandler(new TestHandler());
        server.start();
    }

    public void stop() {
        try {
            server.stop();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getResponseRequestMap() {
        return responseRequestMap;
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    private class TestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse response) throws IOException {
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);

            final String requestString = IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
            final String responseString = RandomStringUtils.randomAlphabetic(16);

            responseRequestMap.put(requestString, responseString);

            response.getWriter().print(responseString);
            request.setHandled(true);
        }
    }
}
