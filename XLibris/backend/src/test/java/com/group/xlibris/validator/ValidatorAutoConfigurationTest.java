package com.group.xlibris.validator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorAutoConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    ValidatorAutoConfiguration.class
                            )
                    );

    @Test
    void shouldRegisterValidatorServiceWhenEnabled() {
        runner
                .withPropertyValues(
                        "xlibris.validator.enabled=true",
                        "xlibris.validator.title=Validation failed",
                        "xlibris.validator.detail=Request contains invalid fields"
                )
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(ValidatorService.class);

                    assertThat(context.getBean(ValidatorService.class))
                            .isInstanceOf(DefaultValidatorService.class);
                });
    }

    @Test
    void shouldNotRegisterValidatorServiceWhenDisabled() {
        runner
                .withPropertyValues(
                        "xlibris.validator.enabled=false",
                        "xlibris.validator.title=Validation failed",
                        "xlibris.validator.detail=Request contains invalid fields"
                )
                .run(context -> {
                    assertThat(context)
                            .doesNotHaveBean(ValidatorService.class);
                });
    }

    @Test
    void shouldNotRegisterValidatorServiceWhenPropertyMissing() {
        runner.run(context -> {
            assertThat(context)
                    .doesNotHaveBean(ValidatorService.class);
        });
    }

    @Test
    void shouldUseCustomValidatorServiceWhenBeanExists() {

        ValidatorService customValidatorService = exception -> null;

        runner
                .withPropertyValues(
                        "xlibris.validator.enabled=true",
                        "xlibris.validator.title=Validation failed",
                        "xlibris.validator.detail=Request contains invalid fields"
                )
                .withBean(
                        ValidatorService.class,
                        () -> customValidatorService
                )
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(ValidatorService.class);

                    assertThat(context.getBean(ValidatorService.class))
                            .isSameAs(customValidatorService);
                });
    }
}