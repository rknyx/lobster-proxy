package com.rk;


import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgParser {
    private static final int ANY = -1;
    private static final String ARROW = "->";
    private static final String SEMICOLON = ";";
    private static final String COLON = ":";
    private static final int MAX_TCP_PORT = 65535;
    private static final String LOCALHOST = "127.0.0.1";

    final String arg;

    public ArgParser(String[] args) {
        Objects.requireNonNull(args);
        this.arg = Stream.of(args)
                .map(StringUtils::trim)
                .collect(Collectors.joining(""));
    }

    public Map<Endpoint, List<Endpoint>> parse() {
        return parseDelimiter(arg, SEMICOLON, ANY)
                .map(this::parseMapping)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(e -> e.getKey(),
                        v -> Collections.singletonList(v.getValue()),
                        ListUtils::union));
    }

    private Map<Endpoint, Endpoint> parseMapping(String str) {
        final Iterator<String> partsIterator = parseDelimiter(str, ARROW, 1).iterator();

        final List<Endpoint> sources = parseSource(partsIterator.next()).collect(Collectors.toList());
        return parseDestinations(partsIterator.next())
                .flatMap(dst -> sources.stream().map(src -> ImmutablePair.of(src, dst)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (a, b) -> b));
    }

    private Stream<Endpoint> parseSource(String src) {
        return parsePorts(src).map(port -> new Endpoint(LOCALHOST, port));
    }

    private Stream<Endpoint> parseDestinations(String src) {
        final Iterator<String> dstIterator = parseDelimiter(src, COLON, 1).iterator();
        final String dstHost = dstIterator.next();

        return parsePorts(dstIterator.next()).map(port -> new Endpoint(dstHost, port));
    }

    private Stream<Integer> parsePorts(String str) {
        return parseDelimiter(str, ",", ANY)
                .map(this::parsePort);
    }

    private Integer parsePort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 0 || portNumber > MAX_TCP_PORT) {
                throw new IllegalArgumentException(String.format("Port number should be in range [0..65535], got '%s'", port));
            }
            return portNumber;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("'%s' should be a valid port", port));
        }
    }

    private Stream<String> parseDelimiter(String str, String delimiter, int count) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(delimiter);

        if (str.isEmpty()) {
            throw new IllegalArgumentException("Arg string cannot be empty");
        }

        if (count != ANY && (!str.contains(delimiter) || str.split(delimiter).length != count + 1)) {
            throw new IllegalArgumentException(String.format("String '%s' is expected to contains '%s' occurence of '%s'",
                    str, count, delimiter));
        }
        if (splitAndTrim(str, delimiter).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException(String.format("Delimiter '%s' should split string '%s' into non-empty parts",
                    delimiter, str));
        }
        return splitAndTrim(str, delimiter);
    }

    private Stream<String> splitAndTrim(String str, String delimiter) {
        return Stream.of(str.split(delimiter))
                .map(StringUtils::trim);
    }
}