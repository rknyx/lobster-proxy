package com.rk.conf;

import picocli.CommandLine.ITypeConverter;

public class PortMappingTypeConverter implements ITypeConverter<PortMapping> {
    public PortMapping convert(String str)  {
        return new PortMappingParser(str).parse();
    }
}
