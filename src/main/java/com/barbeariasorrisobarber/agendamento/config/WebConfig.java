package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Path.of(uploadDir);
        String externalLocation = path.toUri().toString();
        // Serve files under /uploads/** from the configured upload directory on disk
        registry.addResourceHandler("/uploads/**").addResourceLocations(externalLocation);
    }
}
