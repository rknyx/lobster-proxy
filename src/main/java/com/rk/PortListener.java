package com.rk;

import com.rk.domain.Endpoint;
import com.rk.domain.TransmitterResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PortListener implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    private static final Logger dataLogger = LogManager.getLogger("com.rk.DataLogger");

    private final ExecutorService executorService;
    private final Configuration configuration;
    private final Endpoint dstEndpoint;
    private final Endpoint srcEndpoint;

    public PortListener(Endpoint src, Endpoint dst, Configuration configuration) {
        dstEndpoint = dst;
        srcEndpoint = src;
        executorService = Executors.newCachedThreadPool();
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(srcEndpoint.getPort())) {
            while (!Thread.currentThread().isInterrupted()) {
                final Socket inboundSocket = serverSocket.accept();
                logger.info("Acquired one connection on port: '{}'", srcEndpoint.getPort());
                processConnection(inboundSocket);
                logger.info("Closing one connection on port: '{}'", srcEndpoint.getPort());
                inboundSocket.close();
            }
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }

    private void processConnection(Socket inboundSocket) throws InterruptedException {
        try (final Socket outboundSocket = new Socket(dstEndpoint.getHost(), dstEndpoint.getPort())) {
            final List<Future<TransmitterResult>> result = Stream.of(
                    ImmutablePair.of(outboundSocket.getInputStream(), inboundSocket.getOutputStream()),
                    ImmutablePair.of(inboundSocket.getInputStream(), outboundSocket.getOutputStream()))
                    .map(pair -> executorService.submit(() -> this.byteArrayCopy(pair, configuration.logContent)))
                    .collect(Collectors.toList());

            final Iterator<Future<TransmitterResult>> results = result.iterator();
            while (results.hasNext()) {
                results.next().get();
            }
        } catch (ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TransmitterResult byteArrayCopy(Pair<InputStream, OutputStream> pair, boolean logContent) {
        try {
            final InputStream inputStream = pair.getLeft();
            final OutputStream outputStream;
            if (logContent) {
                final OutputStream loggingOutputStream = IoBuilder.forLogger(dataLogger)
                    .setLevel(Level.INFO)
                    .buildOutputStream();
                outputStream = new TeeOutputStream(pair.getRight(), loggingOutputStream);
            } else {
                outputStream = pair.getRight();
            }

            return new TransmitterResult(IOUtils.copy(inputStream, outputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
