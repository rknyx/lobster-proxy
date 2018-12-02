package com.rk;

import com.google.common.collect.ImmutableSet;
import com.rk.domain.Endpoint;
import com.rk.domain.Endpoints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ArgParserTest {

    @Test(expected = NullPointerException.class)
    public void nullArgTest() {
        new ArgParser(null);
    }

    @Test
    public void singlePortMappingTest() {
        final String[] args = "4545 -> 127.0.0.1:1212".split(" ");
        final Set<Endpoints> actual = new ArgParser(args).parse();
        final Set<Endpoints> expected = ImmutableSet.of(
                Endpoints.from(Endpoint.fromLocalhostPort(4545), Endpoint.fromLocalhostPort(1212)));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multiplPortMappingTest() {
        final String[] args = "4545,4546 -> 8.8.8.8:1212,1213".split(" ");
        final Set<Endpoints> actual = new ArgParser(args).parse();
        final Set<Endpoints> expected = ImmutableSet.of(
                Endpoints.from(Endpoint.fromLocalhostPort(4545), Endpoint.from("8.8.8.8", 1212)),
                Endpoints.from(Endpoint.fromLocalhostPort(4545), Endpoint.from("8.8.8.8", 1213)),
                Endpoints.from(Endpoint.fromLocalhostPort(4546), Endpoint.from("8.8.8.8", 1213)),
                Endpoints.from(Endpoint.fromLocalhostPort(4546), Endpoint.from("8.8.8.8", 1212)));
    }
}
