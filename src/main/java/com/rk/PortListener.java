package com.rk;

import com.rk.conf.PortMapping;
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
import java.net.SocketException;
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
    private Boolean logContent;
    private final PortMapping portMapping;
    

    public PortListener(PortMapping portMapping, Boolean logContent) {
        this.portMapping = portMapping;
        this.logContent = logContent;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(portMapping.getSourcePort())) {
            while (!Thread.currentThread().isInterrupted()) {
                final Socket inboundSocket = serverSocket.accept();
                logger.info("Acquired one connection on port: '{}'", portMapping.getSourcePort());
                processConnection(inboundSocket);
                logger.info("Closing one connection on port: '{}'", portMapping.getSourcePort());
                inboundSocket.close();
            }
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }

    private void processConnection(Socket inboundSocket) throws InterruptedException {
        try (final Socket outboundSocket = new Socket(portMapping.getDestHost(), portMapping.getDestPort())) {
            final List<Future<TransmitterResult>> result = Stream.of(
                    ImmutablePair.of(outboundSocket.getInputStream(), inboundSocket.getOutputStream()),
                    ImmutablePair.of(inboundSocket.getInputStream(), outboundSocket.getOutputStream()))
                    .map(pair -> executorService.submit(() -> this.byteArrayCopy(pair, Boolean.TRUE.equals(this.logContent))))
                    .collect(Collectors.toList());

            final Iterator<Future<TransmitterResult>> results = result.iterator();
            while (results.hasNext()) {
                results.next().get();
            }
        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketException) {
                logger.warn("Socket exception while processing {}->{}:{}, message: {}",
                        portMapping.getSourcePort(), portMapping.getDestHost(), portMapping.getDestPort(), e.getMessage());
            }
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TransmitterResult byteArrayCopy(Pair<InputStream, OutputStream> pair, boolean logContent) throws IOException {
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
    }
}
