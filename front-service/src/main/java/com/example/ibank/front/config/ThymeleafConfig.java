package com.example.ibank.front.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;

import java.util.List;

@Configuration
public class ThymeleafConfig {

    @Bean
    public ThymeleafReactiveViewResolver thymeleafReactiveViewResolver( ISpringWebFluxTemplateEngine templateEngine) {
        ThymeleafReactiveViewResolver resolver = new ThymeleafReactiveViewResolver();
        resolver.setTemplateEngine( templateEngine);
        // без этой настройки в Content-Type передавался "text/html" без указания кодировки
        // источник решения: [utf8 charset with Thymeleaf](https://stackoverflow.com/a/71050291/15777370)
        resolver.setSupportedMediaTypes(List.of( MediaType.parseMediaType( "text/html;charset=UTF-8")));
        return resolver;
    }
}
