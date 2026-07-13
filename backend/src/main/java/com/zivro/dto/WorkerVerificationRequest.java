package com.zivro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkerVerificationRequest {

    @NotNull private Boolean verified;
}
