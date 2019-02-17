package com.rk;

import picocli.CommandLine;


public class Lobster {
    public static void main(String[] args) {
        CommandLine.call(new ProxyCommand(), args);
    }
}
