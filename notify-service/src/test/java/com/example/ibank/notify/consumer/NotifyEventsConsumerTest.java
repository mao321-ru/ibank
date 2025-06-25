package com.example.ibank.notify.consumer;

import com.example.ibank.notify.model.Event;
import com.example.ibank.shared.notification.EventCreate;

import com.example.ibank.notify.IntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Slf4j
public class NotifyEventsConsumerTest extends IntegrationTest {

    @Value( "${kafka.topic.notify-events}")
    String notifyEventsTopic;

    @Autowired
    private KafkaTemplate<String, EventCreate> kt;

    @Autowired
    R2dbcEntityTemplate etm;

    @Test
    void createEvent_ok() throws Exception {

        var ec = EventCreate.builder()
            .source( "createEvent_ok")
            .eventType( "create_account")
            .eventTime( OffsetDateTime.parse("2023-05-16T09:15:22+04:00"))
            .userLogin( "createEvent_ok")
            .message( "Открыт счет в RUB - ура")
            .build();

        kt.send( notifyEventsTopic, ec.getSource(), ec).get();
        log.info( "waiting 0.5 seconds after send event ...");
        Thread.sleep( 500);

        var ev = etm.selectOne( query( where( "source").is( ec.getSource())), Event.class).block();
        assertNotNull( "Event not found in DB", ev);
        assertEquals( "Unexpected message", ec.getMessage(), ev.getMessage());
    }

}
