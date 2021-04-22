package com.malsolo.kafka.streams.spring.cloud.stream.transformer;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.purchase.model.avro.RewardAccumulator;
import com.malsolo.kafka.streams.spring.cloud.stream.model.ModelHelper;
import java.util.Objects;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class PurchaseRewardTransformer implements ValueTransformer<Purchase, RewardAccumulator> {
    private final String storeName;
    private KeyValueStore<String, Integer> stateStore;

    public PurchaseRewardTransformer(String storeName) {
        Objects.requireNonNull(storeName,"Store Name can't be null");
        this.storeName = storeName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.stateStore = (KeyValueStore<String, Integer>) context.getStateStore(this.storeName);
    }

    @Override
    public RewardAccumulator transform(Purchase value) {
        var rewardAccumulator = ModelHelper.rewardAccumulatorFromPurchase(value);

        var customerId = rewardAccumulator.getCustomerId();

        var accumulatedSoFar = this.stateStore.get(customerId);

        if (accumulatedSoFar != null) {
            ModelHelper.rewardAccumulatorAddRewardPoints(rewardAccumulator, accumulatedSoFar);
        }

        this.stateStore.put(customerId, accumulatedSoFar);

        return rewardAccumulator;
    }

    @Override
    public void close() {
    }
}