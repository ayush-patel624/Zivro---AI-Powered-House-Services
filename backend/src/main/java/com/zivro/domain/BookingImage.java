package com.zivro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class BookingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "reference_image_url", length = 1024)
    private String referenceImageUrl;

    @Column(name = "reference_public_id", length = 255)
    private String referencePublicId;

    @Column(name = "before_image", length = 1024)
    private String beforeImage;

    @Column(name = "before_public_id", length = 255)
    private String beforePublicId;

    @Column(name = "after_image", length = 1024)
    private String afterImage;

    @Column(name = "after_public_id", length = 255)
    private String afterPublicId;

    @Column(name = "ai_detected_type", length = 64)
    private String aiDetectedType;

    @Column(name = "ai_label", length = 160)
    private String aiLabel;

    @Column(name = "ai_quantity")
    private Integer aiQuantity;

    @Column(name = "ai_quantity_unit", length = 32)
    private String aiQuantityUnit;

    @Column(name = "ai_estimated_minutes")
    private Integer aiEstimatedMinutes;

    @Column(name = "ai_stain_level", length = 32)
    private String aiStainLevel;

    @Column(name = "ai_confidence", precision = 4, scale = 3)
    private BigDecimal aiConfidence;

    @Column(name = "ai_details_json", columnDefinition = "TEXT")
    private String aiDetailsJson;
}
