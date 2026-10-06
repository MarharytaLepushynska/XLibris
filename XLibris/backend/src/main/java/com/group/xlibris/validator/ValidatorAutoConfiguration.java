package com.group.xlibris.validator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ValidatorService.class)
@EnableConfigurationProperties(ValidatorProperties.class)
@ConditionalOnProperty(
        prefix = "xlibris.validator",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ValidatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidatorService validatorService(
            ValidatorProperties properties
    ) {
        return new DefaultValidatorService(properties);
    }
}