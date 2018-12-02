package com.rk;

import com.rk.domain.Endpoint;
import com.rk.domain.TransmitterResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private final ExecutorService executorService;
    private final Endpoint dstEndpoint;
    private final Endpoint srcEndpoint;

    public PortListener(Endpoint src, Endpoint dst, Configuration configuration) {
        dstEndpoint = dst;
        srcEndpoint = src;
        executorService = Executors.newCachedThreadPool();
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

    public void stopListening() {
        Thread.currentThread().interrupt();
    }

    private void processConnection(Socket inboundSocket) throws InterruptedException {
        try (final Socket outboundSocket = new Socket(dstEndpoint.getHost(), dstEndpoint.getPort())) {

            final List<Future<TransmitterResult>> result = Stream.of(
                    ImmutablePair.of(outboundSocket.getInputStream(), inboundSocket.getOutputStream()),
                    ImmutablePair.of(inboundSocket.getInputStream(), outboundSocket.getOutputStream()))
                    .map(pair -> executorService.submit(() -> this.byteArrayCopy(pair)))
                    .collect(Collectors.toList());

            final Iterator<Future<TransmitterResult>> results = result.iterator();
            while (results.hasNext()) {
                results.next().get();
            }
        } catch (ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private TransmitterResult byteArrayCopy(Pair<InputStream, OutputStream> pair) {
        try {
            return new TransmitterResult(IOUtils.copy(pair.getLeft(), pair.getRight()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
