package org.hibersap.generator.manager;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.IOException;

public class Configuration {

    @Produces
    public ConnectionPropertiesManager configure() throws IOException {
        return  new ConnectionPropertiesManager();
    }
}
