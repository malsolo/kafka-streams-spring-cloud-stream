package com.malsolo.kafka.purchase.model;

import com.malsolo.kafka.purchase.model.avro.Purchase;
import com.malsolo.kafka.purchase.model.avro.PurchasePattern;
import com.malsolo.kafka.purchase.model.avro.RewardAccumulator;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

public class ModelHelper {

    private static final String CC_NUMBER_REPLACEMENT="xxxx-xxxx-xxxx-";

    public static Purchase purchaseMaskCreditCard(Purchase purchase) {
        Objects.requireNonNull(purchase.getCreditCardNumber(), "Credit Card can't be null");

        String[] parts = purchase.getCreditCardNumber().split("-");
        if (parts.length < 4 ) {
            purchase.setCreditCardNumber("xxxx");
        } else {
            String last4Digits = purchase.getCreditCardNumber().split("-")[3];
            purchase.setCreditCardNumber(CC_NUMBER_REPLACEMENT + last4Digits);
        }

        return purchase;
    }

    public static PurchasePattern purchasePatternfromPurchase(Purchase purchase) {
        return PurchasePattern.newBuilder()
            .setZipCode(purchase.getZipCode())
            .setItem(purchase.getItemPurchased())
            .setDate(purchase.getPurchaseDate())
            .setAmount(purchase.getPrice() * purchase.getQuantity())
            .build();
    }

    public static RewardAccumulator rewardAccumulatorFromPurchase(Purchase purchase) {
        double purchaseTotal = purchase.getPrice() * (double) purchase.getQuantity();
        return RewardAccumulator.newBuilder()
            .setCustomerId(purchase.getLastName()+","+purchase.getFirstName())
            .setPurchaseTotal(purchaseTotal)
            .setCurrentRewardPoints((int) purchaseTotal)
            //Default values
            .setTotalRewardPoints(0)
            .setDaysFromLastPurchase(0)
            .build();
    }

    public static void rewardAccumulatorAddRewardPoints(RewardAccumulator rewardAccumulator, int previousTotalPoints) {
        rewardAccumulator.setTotalRewardPoints(rewardAccumulator.getTotalRewardPoints() + previousTotalPoints);
    }

    public static long localDateToEpochMillis(LocalDate ld) {
        return ld.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }



}
