package com.project.practice.sap.dto;

import jakarta.validation.constraints.NotNull;

public record ApproveVersionRequest(

    @NotNull(message = "Reviewer ID must be provided")
    Integer reviewerId,

    String comment    // intentionally optional — reviewers may skip the comment
){}
