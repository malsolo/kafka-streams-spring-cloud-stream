package com.malsolo.kafka.streams.spring.cloud.stream.clients;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("purchases.kafka")
@Data
public class PurchasesKafkaProperties {
    private final ProducerProperties producer = new ProducerProperties();
    private final ConsumerProperties consumer = new ConsumerProperties();

    @Data
    public static class ProducerProperties {
        private final ProducerInfo transactions = new ProducerInfo();
    }

    @Data
    public static class ProducerInfo {
        private boolean autoStart;
        private String topic;
        private int numberOfExecutions = 0;
        private int initialDelayInMilliseconds = 750;
        private int delayInMilliseconds = 500;
    }

    @Data
    public static class ConsumerProperties {
        private final ConsumerInfo purchases = new ConsumerInfo();
        private final ConsumerInfo patterns = new ConsumerInfo();
    }

    @Data
    public static class ConsumerInfo {
        private boolean autoStart;
        private String groupId;
        private String topic;
    }

}
