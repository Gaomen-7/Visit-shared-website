package com.travelshare.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 装配：拦截器覆盖 {@code /api/**}；上传目录挂到 {@code /uploads/**} 只读提供。
 *
 * <p>静态图片路径不在 {@code /api} 下，因此不经过鉴权拦截器（有意的公共呈现内容）。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final Path uploadDir;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.jwtInterceptor = jwtInterceptor;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void ensureUploadDir() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString());
    }
}
