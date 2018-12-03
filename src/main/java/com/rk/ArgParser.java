package com.rk;

import com.google.common.collect.ImmutableSet;
import com.rk.domain.Endpoint;
import com.rk.domain.Endpoints;
import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgParser {
    private static final int ANY = -1;
    private static final String ARROW = "->";
    private static final String SEMICOLON = ";";
    private static final String COLON = ":";
    private static final int MAX_TCP_PORT = 65535;
    private static final String LOCALHOST = "127.0.0.1";

    public class ArgParseException extends RuntimeException {
        public ArgParseException(String msg) {
            super(msg);
        }
    }

    final String arg;

    public ArgParser(String[] args) {
        Objects.requireNonNull(args);
        this.arg = Stream.of(args)
                .map(StringUtils::trim)
                .collect(Collectors.joining(""));
    }

    public Set<Endpoints> parse() {
        return parseDelimiter(arg, SEMICOLON, ANY)
                .map(this::parseMapping)
                .reduce((a, b) -> ImmutableSet.<Endpoints>builder().addAll(a).addAll(b).build())
                .orElse(Collections.emptySet());
    }

    private Set<Endpoints> parseMapping(String str) {
        final Iterator<String> partsIterator = parseDelimiter(str, ARROW, 1).iterator();

        final List<Endpoint> sources = parseSource(partsIterator.next()).collect(Collectors.toList());

        return parseDestinations(partsIterator.next())
                .flatMap(dst -> sources.stream().map(src -> Endpoints.from(src, dst)))
                .collect(Collectors.toSet());
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
                throw new ArgParseException(String.format("Port number should be in range [0..65535], got '%s'", port));
            }
            return portNumber;
        } catch (NumberFormatException e) {
            throw new ArgParseException(String.format("'%s' should be a valid port", port));
        }
    }

    private Stream<String> parseDelimiter(String str, String delimiter, int count) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(delimiter);

        if (str.isEmpty()) {
            throw new ArgParseException("Arg string cannot be empty");
        }

        if (count != ANY && (!str.contains(delimiter) || str.split(delimiter).length != count + 1)) {
            throw new ArgParseException(String.format("String '%s' is expected to contains '%s' occurence of '%s'",
                    str, count, delimiter));
        }
        if (splitAndTrim(str, delimiter).anyMatch(StringUtils::isBlank)) {
            throw new ArgParseException(String.format("Delimiter '%s' should split string '%s' into non-empty parts",
                    delimiter, str));
        }
        return splitAndTrim(str, delimiter);
    }

    private Stream<String> splitAndTrim(String str, String delimiter) {
        return Stream.of(str.split(delimiter))
                .map(StringUtils::trim);
    }
}