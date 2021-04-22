package com.malsolo.kafka.streams.spring.cloud.stream.clients.consumer;

import com.malsolo.kafka.purchase.model.avro.RewardAccumulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RewardsConsumer {

    @KafkaListener(id = "RewardsConsumer",
        autoStartup = "${purchases.kafka.consumer.rewards.auto-start}",
        topics = "${purchases.kafka.consumer.rewards.topic}",
        groupId = "${purchases.kafka.consumer.rewards.group-id}",
        clientIdPrefix = "RewardsConsumer-prefix"
    )
    public void consume(RewardAccumulator rewardAccumulator) {
        log.info("##### Rewards received: {}", rewardAccumulator);
    }

}
