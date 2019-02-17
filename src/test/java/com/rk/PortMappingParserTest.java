package com.rk;

import com.rk.conf.PortMapping;
import com.rk.conf.PortMappingParser;
import org.junit.Assert;
import org.junit.Test;


public class PortMappingParserTest {

    @Test
    public void singlePortMappingTest() {
        final PortMapping actual = new PortMappingParser("4545 -> 127.0.0.1:1212").parse();
        final PortMapping expected = new PortMapping(4545, "127.0.0.1", 1212);

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = PortMappingParser.ArgParseException.class)
    public void incorrectArgsTest() {
        new PortMappingParser("Hello World").parse();
    }
}
