package com.project.practice.sap.dto;

public record ApproveVersionRequest (
        Integer reviewerId,     // userId
        String comment         // optional
){}
