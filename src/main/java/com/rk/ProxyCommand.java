package com.rk;

import com.rk.conf.PortMapping;
import com.rk.conf.PortMappingTypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Command(name = "proxy",
        header = "Start proxy ports listening",
        mixinStandardHelpOptions = true,
        version = "Lobster proxy 0.9")
public class ProxyCommand implements Callable<Void> {
    @Parameters(index = "0", converter = PortMappingTypeConverter.class)
    private Set<PortMapping> endpointsMapping;

    @Option(names = {"-l", "--logContent"},
            description = "Log content of tcp packets")
    private Boolean logContent;

    @Option(names = {"-s", "--strobe"},
            description = "a" +
                    "Automatically switch establish and break connection with interval")
    private Long strobe;

    private static final Logger logger = LogManager.getLogger();
    private static final Long TERMINATION_TIMEOUT_MILLISECONDS = 2000L;
    private ExecutorService listenersPool;
    private ScheduledExecutorService strobeExecutor;


    public Void call() {
        if (strobe != null) {
            strobeExecutor = Executors.newSingleThreadScheduledExecutor();
            strobeExecutor.scheduleWithFixedDelay(this::doStrobe, strobe, strobe, TimeUnit.MILLISECONDS);
        }

        startProxyListeners();

        return null;
    }

    void startProxyListeners() {
        listenersPool = Executors.newCachedThreadPool();
        endpointsMapping.forEach(portMapping -> listenersPool.submit(new PortListener(portMapping, logContent)));
    }

    void stopProxyListeners() throws InterruptedException {
        listenersPool.shutdownNow();
        listenersPool.awaitTermination(TERMINATION_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void terminateProxy() {
        logger.debug("Terminating proxy listeners...");

        Optional.ofNullable(listenersPool).ifPresent(ExecutorService::shutdownNow);
        Optional.ofNullable(strobeExecutor).ifPresent(ExecutorService::shutdownNow);
    }

    private void doStrobe() {
        try {
            stopProxyListeners();
            startProxyListeners();
        } catch (InterruptedException e) {
            terminateProxy();
        }
    }

    public void setEndpointsMapping(Set<PortMapping> endpointsMapping) {
        this.endpointsMapping = endpointsMapping;
    }

    public void setLogContent(Boolean logContent) {
        this.logContent = logContent;
    }

    public void setStrobe(Long strobe) {
        this.strobe = strobe;
    }
}
