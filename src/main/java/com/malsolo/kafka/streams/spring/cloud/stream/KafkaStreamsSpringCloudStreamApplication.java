package com.malsolo.kafka.streams.spring.cloud.stream;

import static com.malsolo.kafka.purchase.model.ModelHelper.localDateToEpochMillis;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.purchase.model.ModelHelper;
import java.util.function.Function;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsSpringCloudStreamApplication {

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

}
