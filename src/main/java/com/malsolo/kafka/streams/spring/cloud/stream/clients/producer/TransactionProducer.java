package com.malsolo.kafka.streams.spring.cloud.stream.clients.producer;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.streams.spring.cloud.stream.clients.PurchasesKafkaProperties;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionProducer {

    private final PurchasesKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Purchase> kafkaTemplate;

    public void sendPurchase() {
        var max = kafkaProperties.getProducer().getTransactions().getNumberOfExecutions();
        if (max > 0) {
            long delayMs = kafkaProperties.getProducer().getTransactions().getInitialDelayInMilliseconds();
            long periodMs = kafkaProperties.getProducer().getTransactions().getDelayInMilliseconds();
            Flux.interval(Duration.ofMillis(delayMs), Duration.ofMillis(periodMs))
                .doOnNext(this::createAndSendPurchase)
                .take(max)
                .subscribe();
        }
    }

    private Purchase createAndSendPurchase(long ignored) {
        Purchase purchase = Generator.generatePurchase();
        log.info("Transaction-Purchase created {}", purchase);

        kafkaTemplate.send(kafkaProperties.getProducer().getTransactions().getTopic(), purchase)
            .addCallback(new KafkaSendCallback<>(){
                @Override
                public void onSuccess(SendResult<String, Purchase> result) {
                    var metadata = result.getRecordMetadata();
                    log.info("Transaction-Purchase record sent successfully, to topic {}, partition {}, and offset {}: {}",
                        metadata.topic(), metadata.partition(), metadata.offset(), result.getProducerRecord().value());
                }
                @Override
                public void onFailure(KafkaProducerException e) {
                    log.error("Transaction-Purchase Sent failed: {}", e.getMessage());
                }
            });
        log.info("Transaction-Purchase produced {}.", purchase);

        return purchase;
    }

}
