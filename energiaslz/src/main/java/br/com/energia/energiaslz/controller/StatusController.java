package br.com.energia.energiaslz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public String status() {
        return "API está funcionando! - " + new java.util.Date();
    }

    @GetMapping
    public String home() {
        return "Bem-vindo à API Energia SLZ!";
    }
}
