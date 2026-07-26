package com.example.demo.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aspose.gridjs.GridJsOptions;

@Configuration
public class GridJsConfig {

    @Value("${app.cache-directory}")
    private String cacheDirectory;

    @Bean
    public GridJsOptions gridJsOptions() throws Exception {
        Path cachePath = Paths.get(cacheDirectory).toAbsolutePath().normalize();
        Files.createDirectories(cachePath);

        GridJsOptions options = new GridJsOptions();
        options.setFileCacheDirectory(cachePath.toString());
        options.setBaseRouteName("/api/excel");
        return options;
    }
}
