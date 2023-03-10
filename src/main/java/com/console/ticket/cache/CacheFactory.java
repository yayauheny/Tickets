package com.console.ticket.cache;

import com.console.ticket.exception.FileException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CacheFactory {
    private String ymlFileName = "application.yml";
    private static int capacity;
    private static CacheType type;

    public Cache getCache(int capacity, CacheType type) {
        switch (type) {
            case LRU:
                return new LruCache(capacity);
            default:
                return new LfuCache(capacity);
        }
    }

    public Cache getCacheFromYml() {
        readYmlFile(ymlFileName);
        switch (type) {
            case LRU:
                return new LruCache(capacity);
            default:
                return new LfuCache(capacity);
        }
    }

    private static void readYmlFile(String resourceName) {
        Map<String, Object> data;
        try (InputStream inputStream = CacheFactory.class.getClassLoader().getResourceAsStream(resourceName)) {
            Yaml yaml = new Yaml();
            data = yaml.load(inputStream);
        } catch (IOException e) {
            throw new FileException("Exception read " + resourceName + " file: ", e);
        }

        capacity = (int) data.get("capacity");

        type = CacheType.valueOf((String) data.get("type"));
    }
}