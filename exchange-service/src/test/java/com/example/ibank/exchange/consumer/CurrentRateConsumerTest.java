package com.example.ibank.exchange.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.ibank.exchange.IntegrationTest;
import com.example.ibank.exchange.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

@Slf4j
public class CurrentRateConsumerTest extends IntegrationTest {

    @Value( "${kafka.topic.current-rates}")
    String currentRatesTopic;

    @Autowired
    private KafkaTemplate<String, String> kt;

    @Autowired
    private ExchangeService srv;

    @Test
    void setRates_ok() throws Exception {
        final String usdRateStr = "50.91";

        // меняем
        kt.send( currentRatesTopic, "USD", usdRateStr).get();
        log.info( "waiting 0.5 seconds after send new USD rate {} ...", usdRateStr);
        Thread.sleep( 500);

        // проверяем
        var rates = srv.getRates().collectList().block();
        assertThat( rates)
            .withFailMessage( "New USD rate %s not found in getRates: %s", usdRateStr, rates)
            .anyMatch(rate ->
                "USD".equals(rate.getCurrencyCode()) &&
                    new BigDecimal( usdRateStr).compareTo(rate.getRate()) == 0
            )
        ;

        // восстанавливаем начальный USD чтобы не повлиять на другие тесты
        kt.send( currentRatesTopic, "USD", INITIAL_USD_RATE).get();
        log.info( "waiting 0.5 seconds after restore initial rate...");
        Thread.sleep( 500);
    }

}
