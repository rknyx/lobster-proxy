package com.rk;

import com.google.common.util.concurrent.Uninterruptibles;
import com.rk.conf.PortMapping;
import com.rk.util.HttpClientWrapper;
import com.rk.util.HttpServerWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HttpBasedTest {
    private static int initialPort = 64333;
    private static int threadCount = 40;
    private static int negotiationCount = 80;

    private List<HttpTestContext> contexts;
    private final ProxyCommand proxyCommand = new ProxyCommand();


    @Before
    public void setUp() {
        contexts = IntStream.range(0, threadCount)
                .mapToObj(i -> HttpBasedTest.initializeTestContext(new PortMapping(initialPort++, PortMapping.LOCALHOST, initialPort++)))
                .collect(Collectors.toList());

        proxyCommand.setEndpointsMapping(contexts.stream().map(HttpTestContext::getPortMapping).collect(Collectors.toSet()));
        proxyCommand.startProxyListeners();
        proxyCommand.setLogContent(true);

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        contexts.forEach(HttpBasedTest::tearDownTestContext);
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    public void testTransmission() throws Exception {
        final List<Future<?>> futures = IntStream.range(0, negotiationCount)
                .mapToObj(i -> contexts.stream().map(c -> c.getExecutorService().submit(() -> c.getHttpClient().send())))
                .flatMap(Function.identity())
                .collect(Collectors.toList());

        contexts.forEach(c -> c.getExecutorService().shutdown());
        for (Future<?> future : futures) {
            future.get();
        }

        contexts.forEach(c ->
            Assert.assertEquals(c.getHttpClient().getRequestResponseMap(), c.getTestServer().getResponseRequestMap())
        );
    }

    private static HttpTestContext initializeTestContext(PortMapping portMapping) {
        try {
            final HttpTestContext context = new HttpTestContext();
            context.setHttpClient(new HttpClientWrapper(portMapping));
            context.setPortMapping(portMapping);
            context.setTestServer(new HttpServerWrapper(portMapping.getDestPort()));
            context.getTestServer().start();
            context.setExecutorService(Executors.newSingleThreadExecutor());
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void tearDownTestContext(HttpTestContext httpTestContext) {
        try {
            httpTestContext.getHttpClient().close();
            httpTestContext.getTestServer().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class HttpTestContext {
        private PortMapping portMapping;
        private HttpServerWrapper testServer;
        private HttpClientWrapper httpClient;
        private ExecutorService executorService;

        public void setPortMapping(PortMapping portMapping) {
            this.portMapping = portMapping;
        }

        public void setTestServer(HttpServerWrapper testServer) {
            this.testServer = testServer;
        }

        public void setHttpClient(HttpClientWrapper httpClient) {
            this.httpClient = httpClient;
        }

        public PortMapping getPortMapping() {
            return portMapping;
        }

        public HttpServerWrapper getTestServer() {
            return testServer;
        }

        public HttpClientWrapper getHttpClient() {
            return httpClient;
        }

        public ExecutorService getExecutorService() {
            return executorService;
        }

        public void setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
        }
    }
}
