package com.malsolo.kafka.streams.spring.cloud.stream.clients.consumer;

import com.malsolo.kafka.purchase.model.avro.PurchasePattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchasePatternConsumer {

    @KafkaListener(id = "PurchasePatternConsumer",
        autoStartup = "${purchases.kafka.consumer.patterns.auto-start}",
        topics = "${purchases.kafka.consumer.patterns.topic}",
        groupId = "${purchases.kafka.consumer.patterns.group-id}",
        clientIdPrefix = "PurchasePatternConsumer-prefix"
    )
    public void consume(PurchasePattern purchasePattern) {
        log.info(">>>>> Purchase Pattern received: {}", purchasePattern);
    }

}
