package com.malsolo.kafka.streams.spring.cloud.stream;

import static com.malsolo.kafka.streams.spring.cloud.stream.model.ModelHelper.localDateToEpochMillis;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.purchase.model.avro.PurchasePattern;
import com.malsolo.kafka.purchase.model.avro.RewardAccumulator;
import com.malsolo.kafka.streams.spring.cloud.stream.clients.producer.TransactionProducer;
import com.malsolo.kafka.streams.spring.cloud.stream.clients.PurchasesKafkaProperties;
import com.malsolo.kafka.streams.spring.cloud.stream.model.ModelHelper;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({PurchasesKafkaProperties.class})
@Slf4j
public class KafkaStreamsSpringCloudStreamApplication {

	public static final String EMPLOYEE_ID = "000000";

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsSpringCloudStreamApplication.class, args);
	}

	@Bean
	public Function<KStream<String, Purchase>, KStream<String, Purchase>> purchasesProcessor() {
		KeyValueMapper<String, Purchase, String> purchaseDateAsKey = (key, purchase) ->
			String.valueOf(localDateToEpochMillis(purchase.getPurchaseDate()));
		return stream -> stream
			.mapValues(ModelHelper::purchaseMaskCreditCard)
			.filter((key, purchase) -> purchase.getPrice() > 5.00)
			.selectKey(purchaseDateAsKey);
	}

	@Bean
	public Function<KStream<String, Purchase>, KStream<String, PurchasePattern>> patternsProcessor() {
		return stream -> stream
			.mapValues(ModelHelper::purchaseMaskCreditCard)
			.mapValues(ModelHelper::purchasePatternfromPurchase);
	}

	@Bean
	public Function<KStream<String, Purchase>, KStream<String, Purchase>> purchasesKeySelectorProcessor() {
		return stream -> stream
			.selectKey((key, purchase) -> purchase.getCustomerId())
			.mapValues(ModelHelper::purchaseMaskCreditCard);
	}

	//END KAFKA STREAMS

	@Bean
	public ApplicationRunner producePurchases(PurchasesKafkaProperties kafkaProperties, TransactionProducer transactionProducer) {
		return args -> {
			if (kafkaProperties.getProducer().getTransactions().isAutoStart()) {
				transactionProducer.sendPurchase();
			}
			else {
				log.warn("***** No purchases are produced because auto start is disabled");
			}
		};
	}

}
