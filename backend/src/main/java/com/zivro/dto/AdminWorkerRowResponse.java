package com.zivro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminWorkerRowResponse {
    Long id;
    String employeeId;
    boolean verified;
    String userName;
    String userEmail;
}
