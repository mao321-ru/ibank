package com.example.ibank.exrate.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
// обеспечиваем возможность отключения запуска по расписанию (используется в тестах)
@ConditionalOnProperty( value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
