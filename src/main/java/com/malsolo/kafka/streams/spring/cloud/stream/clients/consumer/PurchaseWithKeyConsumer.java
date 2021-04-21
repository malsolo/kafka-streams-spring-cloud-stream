package com.malsolo.kafka.streams.spring.cloud.stream.clients.consumer;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseWithKeyConsumer {

    @KafkaListener(id = "PurchaseWithKeyConsumer",
        autoStartup = "${purchases.kafka.consumer.purchaseskeyed.auto-start}",
        topics = "${purchases.kafka.consumer.purchaseskeyed.topic}",
        groupId = "${purchases.kafka.consumer.purchaseskeyed.group-id}",
        clientIdPrefix = "PurchaseWithKeyConsumer-prefix"
    )
    public void consume(ConsumerRecord<String, Purchase> purchaseRecord) {
        log.info(">>>>> PurchaseWithKeyConsumer with key {} received: {}",
            purchaseRecord.key(), purchaseRecord.value());
    }

}
