package com.malsolo.kafka.streams.spring.cloud.stream;

import static com.malsolo.kafka.streams.spring.cloud.stream.model.ModelHelper.localDateToEpochMillis;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.purchase.model.avro.PurchasePattern;
import com.malsolo.kafka.purchase.model.avro.RewardAccumulator;
import com.malsolo.kafka.streams.spring.cloud.stream.clients.producer.TransactionProducer;
import com.malsolo.kafka.streams.spring.cloud.stream.clients.PurchasesKafkaProperties;
import com.malsolo.kafka.streams.spring.cloud.stream.model.ModelHelper;
import com.malsolo.kafka.streams.spring.cloud.stream.transformer.PurchaseRewardTransformer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${streams.state-store.rewards-points}")
	private String rewardsPointsStateStoreName;


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

	@Bean
	public StoreBuilder<KeyValueStore<String, Integer>> rewardsPointsStoreBuilder() {
		return Stores.keyValueStoreBuilder(
			Stores.inMemoryKeyValueStore(rewardsPointsStateStoreName),
			Serdes.String(),
			Serdes.Integer()
		);
	}

	@Bean
	public PurchaseRewardTransformer purchaseRewardTransformer() {
		return new PurchaseRewardTransformer(rewardsPointsStateStoreName);
	}

	@Bean
	public Function<KStream<String, Purchase>, KStream<String, RewardAccumulator>> rewardsProcessor(PurchaseRewardTransformer purchaseRewardTransformer) {
		return stream -> stream
			.transformValues(() -> purchaseRewardTransformer, rewardsPointsStateStoreName);
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
