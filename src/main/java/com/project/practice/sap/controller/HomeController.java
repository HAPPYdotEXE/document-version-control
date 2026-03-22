package com.project.practice.sap.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/status")
    public String home() {
        return "SAP Document Version Control API is running";
    }
}
