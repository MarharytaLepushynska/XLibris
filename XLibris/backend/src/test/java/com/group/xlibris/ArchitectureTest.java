package com.group.xlibris;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ArchitectureTest {
    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(XLibrisApplication.class).verify();
    }
}
