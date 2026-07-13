package com.zivro.domain;

public enum PaymentStatus {
    /** Razorpay not configured — booking proceeds without online payment. */
    NOT_CONFIGURED,
    /** Deposit order created; awaiting customer payment. */
    PENDING,
    /** Deposit captured; balance may be due after satisfaction pricing. */
    PARTIALLY_PAID,
    /** All required amounts captured. */
    PAID,
    FAILED
}
