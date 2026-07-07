package com.collecte.projetCIL.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.request.MotDePasseOublieRequest;
import com.collecte.projetCIL.dto.request.ReinitialiserMotDePasseRequest;
import com.collecte.projetCIL.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints publics de réinitialisation de mot de passe (aucune
 * authentification requise — voir SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * POST /api/auth/mot-de-passe-oublie
     * Corps : { "email": "..." }
     * Envoie un email contenant un lien de réinitialisation (valable 30 min).
     */
    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<Map<String, String>> motDePasseOublie(@RequestBody MotDePasseOublieRequest request) {
        passwordResetService.demanderReinitialisation(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email, un lien de réinitialisation vient de lui être envoyé."
        ));
    }

    /**
     * POST /api/auth/reinitialiser-mot-de-passe
     * Corps : { "token": "...", "nouveauMotDePasse": "..." }
     */
    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<Map<String, String>> reinitialiserMotDePasse(@RequestBody ReinitialiserMotDePasseRequest request) {
        passwordResetService.reinitialiserMotDePasse(request.getToken(), request.getNouveauMotDePasse());
        return ResponseEntity.ok(Map.of(
                "message", "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter."
        ));
    }
}
