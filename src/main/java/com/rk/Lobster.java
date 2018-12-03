package com.rk;


import com.google.common.io.Resources;
import com.rk.domain.Endpoints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

public class Lobster {
    private static final Logger logger = LogManager.getLogger();
    private static final String PARSE_ERROR = "Unable to parse command line arguments: '{}'";

    public static void main(String[] args) {
        try {
            logger.info(Lobster::banner);
            final Set<Endpoints> endpoints = new ArgParser(args).parse();
            final Configuration configuration = new Configuration(endpoints, true);
            endpoints.forEach(p -> new PortListener(p.getSrc(), p.getDst(), configuration).run());

        } catch (ArgParser.ArgParseException e) {
            logger.error(PARSE_ERROR, e.getMessage());
            logger.info(Lobster::usage);
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("UnstableApiUsage")
    private static String banner() {
        try {
            return Resources.toString(Resources.getResource("banner.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static String usage() {
        try {
            return Resources.toString(Resources.getResource("help.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
