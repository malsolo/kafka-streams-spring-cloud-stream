package com.malsolo.kafka.streams.spring.cloud.stream.clients;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseConsumer {

    @KafkaListener(id = "PurchaseConsumer",
        autoStartup = "${purchases.kafka.consumer.purchases.auto-start}",
        topics = "${purchases.kafka.consumer.purchases.topic}",
        groupId = "${purchases.kafka.consumer.purchases.group-id}",
        clientIdPrefix = "PurchaseConsumer"
    )
    public void consume(Purchase purchase) {
        log.info(">>>>> Purchase received: {}", purchase);
    }

}
