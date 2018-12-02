package com.rk;


import com.rk.domain.Endpoints;

import java.util.Set;

public class App {
    public static void main(String[] args) {
        final Set<Endpoints> endpoints = new ArgParser(args).parse(); //todo try catch

        final Configuration configuration = new Configuration(endpoints, false);

        endpoints.forEach(p -> new PortListener(p.getSrc(), p.getDst(), configuration).run());
    }
}
