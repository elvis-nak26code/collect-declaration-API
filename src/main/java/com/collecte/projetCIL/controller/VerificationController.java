package com.collecte.projetCIL.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.service.VerificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * GET /api/verification/fonction?email=xxx@sofitex.bf
     */
    @GetMapping("/fonction")
    public ResponseEntity<Map<String, String>> getFonction(@RequestParam String email) {
        return ResponseEntity.ok(verificationService.getFonctionByEmail(email));
    }
}