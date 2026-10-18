package org.example.converterservice.config;

import org.example.converterservice.service.convert.ConverterStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ConvertersConfig {
    @Bean
    public Map<String, ConverterStrategy> converters(List<ConverterStrategy> all) {
        Map<String, ConverterStrategy> map = new HashMap<>();
        for(ConverterStrategy s : all) {
            map.put(s.extension(), s);
        }
        return map;
    }
}
