package com.rk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArgParserTest {

    @Test(expected = NullPointerException.class)
    public void nullArgTest() {
        new ArgParser(null);
    }

    @Test
    public void singlePortMappingTest() {
        final String[] args = "4545 -> 127.0.0.1:1212".split(" ");
        final Map<Endpoint, List<Endpoint>> res = new ArgParser(args).parse();
        Assert.assertEquals(ImmutableMap.of(
                new Endpoint("127.0.0.1", 4545),
                Collections.singletonList(new Endpoint("127.0.0.1", 1212))
        ), res);
    }

    @Test
    public void multiplPortMappingTest() {
        final String[] args = "4545,4546 -> 127.0.0.1:1212,1213".split(" ");
        final Map<Endpoint, List<Endpoint>> res = new ArgParser(args).parse();
        Assert.assertEquals(ImmutableMap.of(
                new Endpoint("127.0.0.1", 4545),
                ImmutableList.of(new Endpoint("127.0.0.1", 1212), new Endpoint("127.0.0.1", 1213)),
                new Endpoint("127.0.0.1", 4546),
                ImmutableList.of(new Endpoint("127.0.0.1", 1212), new Endpoint("127.0.0.1", 1213))
        ), res);
    }
}
