package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.InscriptionRequest;
import com.collecte.projetCIL.dto.request.LoginRequest;
import com.collecte.projetCIL.dto.response.AuthResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.service.AuthService;
import com.collecte.projetCIL.service.InscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final InscriptionService inscriptionService;

    // Login (admin + utilisateurs validés)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, extraireIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    // Inscription publique -> crée une demande EN_ATTENTE
    @PostMapping("/inscription")
    public ResponseEntity<MessageResponse> inscrire(@RequestBody InscriptionRequest request) {
        return ResponseEntity.ok(inscriptionService.inscrire(request));
    }

    /** Récupère la vraie IP du client, y compris derrière un proxy/reverse-proxy (X-Forwarded-For). */
    private String extraireIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
