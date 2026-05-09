package org.lb4.loadbalancer.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private ConfigLoader() {
    }

    public static AppConfig loadFromResource(String resourcePath) {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Config resource not found: " + resourcePath);
            }
            return read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config from resource: " + resourcePath, e);
        }
    }

    public static AppConfig loadFromPath(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config from path: " + path, e);
        }
    }

    private static AppConfig read(InputStream in) throws IOException {
        AppConfig config = MAPPER.readValue(in, AppConfig.class);
        config.validate();
        return config;
    }
}
