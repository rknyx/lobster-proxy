package com.rk;


import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        final Map<Endpoint, List<Endpoint>> portsMap = new ArgParser(args).parse(); //todo try catch

        final Configuration configuration = new Configuration(portsMap, false);

        portsMap.forEach((k, v) -> v.forEach(dst -> new PortListener(k, dst, configuration).run()));
    }
}
