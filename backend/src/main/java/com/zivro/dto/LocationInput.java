package com.zivro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationInput {

    @NotBlank
    private String address;

    /** e.g. "Current location" or "Other location" */
    private String label;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
