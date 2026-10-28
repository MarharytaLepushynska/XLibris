package com.group.xlibris.e2e;

import com.group.xlibris.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public @interface E2ETest {}

