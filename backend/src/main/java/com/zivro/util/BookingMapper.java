package com.zivro.util;

import com.zivro.domain.Booking;
import com.zivro.domain.BookingImage;
import com.zivro.domain.PaymentStatus;
import com.zivro.domain.Rating;
import com.zivro.dto.BookingImageResponse;
import com.zivro.dto.BookingResponse;
import com.zivro.dto.ImageAnalysisResponse;
import com.zivro.dto.RatingResponse;
import com.zivro.dto.ServiceResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.util.StringUtils;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .userId(b.getUser().getId())
                .workerId(b.getWorker() != null ? b.getWorker().getId() : null)
                .workerEmployeeId(b.getWorker() != null ? b.getWorker().getEmployeeId() : null)
                .service(b.getService() != null ? toServiceResponse(b.getService()) : null)
                .status(b.getStatus())
                .price(b.getPrice())
                .bookingTime(b.getBookingTime())
                .urgencyLevel(b.getUrgencyLevel())
                .images(toImageResponse(b.getBookingImage()))
                .rating(toRatingResponse(b.getRating()))
                .paymentStatus(b.getPaymentStatus())
                .depositAmount(b.getDepositAmount())
                .amountPaid(b.getAmountPaid())
                .finalPriceAfterSatisfaction(b.getFinalPriceAfterSatisfaction())
                .activeRazorpayOrderId(resolveActiveOrderId(b))
                .amountDueNext(resolveAmountDueNext(b))
                .razorpayDepositOrderId(b.getRazorpayOrderId())
                .razorpayBalanceOrderId(b.getRazorpayBalanceOrderId())
                .serviceAddress(b.getServiceAddress())
                .locationLabel(b.getLocationLabel())
                .latitude(b.getLatitude() != null ? b.getLatitude().doubleValue() : null)
                .longitude(b.getLongitude() != null ? b.getLongitude().doubleValue() : null)
                .mapsUrl(buildMapsUrl(b))
                .build();
    }

    private static String buildMapsUrl(Booking b) {
        if (b.getLatitude() == null || b.getLongitude() == null) {
            return null;
        }
        return "https://www.google.com/maps/search/?api=1&query="
                + b.getLatitude().stripTrailingZeros().toPlainString()
                + ","
                + b.getLongitude().stripTrailingZeros().toPlainString();
    }

    private static String resolveActiveOrderId(Booking b) {
        if (StringUtils.hasText(b.getRazorpayBalanceOrderId())) {
            return b.getRazorpayBalanceOrderId();
        }
        if (b.getPaymentStatus() == PaymentStatus.PENDING && StringUtils.hasText(b.getRazorpayOrderId())) {
            return b.getRazorpayOrderId();
        }
        return null;
    }

    private static BigDecimal resolveAmountDueNext(Booking b) {
        if (StringUtils.hasText(b.getRazorpayBalanceOrderId())
                && b.getFinalPriceAfterSatisfaction() != null
                && b.getAmountPaid() != null) {
            return b.getFinalPriceAfterSatisfaction()
                    .subtract(b.getAmountPaid())
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (b.getPaymentStatus() == PaymentStatus.PENDING && b.getDepositAmount() != null) {
            return b.getDepositAmount();
        }
        return null;
    }

    private static BookingImageResponse toImageResponse(BookingImage bi) {
        if (bi == null) {
            return null;
        }
        ImageAnalysisResponse ai = null;
        if (bi.getAiDetectedType() != null) {
            ai =
                    ImageAnalysisResponse.builder()
                            .detectedType(bi.getAiDetectedType())
                            .label(bi.getAiLabel())
                            .quantity(bi.getAiQuantity())
                            .quantityUnit(bi.getAiQuantityUnit())
                            .estimatedMinutes(bi.getAiEstimatedMinutes())
                            .stainLevel(bi.getAiStainLevel())
                            .confidence(bi.getAiConfidence() != null ? bi.getAiConfidence().doubleValue() : null)
                            .summary(bi.getAiDetailsJson())
                            .build();
        }
        return BookingImageResponse.builder()
                .referenceImageUrl(bi.getReferenceImageUrl())
                .beforeWorkImageUrl(bi.getBeforeImage())
                .afterWorkImageUrl(bi.getAfterImage())
                .aiAnalysis(ai)
                .build();
    }

    private static RatingResponse toRatingResponse(Rating r) {
        if (r == null) {
            return null;
        }
        Integer sat = r.getSatisfactionStars();
        return RatingResponse.builder()
                .workerStars(r.getStars())
                .satisfactionStars(sat != null ? sat : 0)
                .feedback(r.getFeedback())
                .build();
    }

    public static ServiceResponse toServiceResponse(com.zivro.domain.ServiceCatalog s) {
        return ServiceResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .basePrice(s.getBasePrice())
                .category(s.getCategory())
                .iconKey(s.getIconKey())
                .sortOrder(s.getSortOrder())
                .build();
    }
}
