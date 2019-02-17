package com.rk.conf;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class PortMappingParser {
    private static final int ANY = -1;
    private static final String ARROW = "->";
    private static final String COLON = ":";
    private static final String LOCALHOST = "127.0.0.1";

    public class ArgParseException extends RuntimeException {
        public ArgParseException(String msg) {
            super(msg);
        }
    }

    final String arg;

    public PortMappingParser(String args) {
        this.arg = args;
    }

    public PortMapping parse() {
        return parseMapping(arg);
    }

    private PortMapping parseMapping(String str) {
        final Iterator<String> mappingParts = parseDelimiter(str, ARROW, 1).iterator();
        Integer srcPort = parsePort(mappingParts.next());

        final Iterator<String> destParts = parseDelimiter(mappingParts.next(), COLON, 1).iterator();
        String destHost = destParts.next();
        Integer destPort = parsePort(destParts.next());

        return new PortMapping(srcPort, destHost, destPort);
    }

    private Integer parsePort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 0 || portNumber > PortMapping.PORT_MAX) {
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