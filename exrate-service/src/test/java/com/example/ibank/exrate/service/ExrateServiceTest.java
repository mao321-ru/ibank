package com.example.ibank.exrate.service;

import com.example.ibank.exrate.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

//@TestPropertySource( properties = "spring.task.scheduling.enabled=false")
public class ExrateServiceTest extends IntegrationTest {

    @Autowired
    ExrateService srv;

    @Test
    void refreshRates_ok() throws Exception {
        assertThat( srv.refreshRates()).as( "Bad execution result").isTrue();
    }

}
