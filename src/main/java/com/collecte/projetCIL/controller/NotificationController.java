package com.collecte.projetCIL.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.response.NotificationResponse;
import com.collecte.projetCIL.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * Routes notifications.
 *
 * GET    /api/notifications/{utilisateurId}              → toutes les notifs de l'utilisateur
 * GET    /api/notifications/{utilisateurId}/non-lues     → uniquement les non lues
 * PATCH  /api/notifications/{id}/lire                   → marquer une notif comme lue
 * PATCH  /api/notifications/{utilisateurId}/lire-tout   → marquer toutes comme lues
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{utilisateurId}")
    public ResponseEntity<List<NotificationResponse>> listerParUtilisateur(
            @PathVariable Long utilisateurId) {
        return ResponseEntity.ok(notificationService.listerParUtilisateur(utilisateurId));
    }

    @GetMapping("/{utilisateurId}/non-lues")
    public ResponseEntity<List<NotificationResponse>> listerNonLues(
            @PathVariable Long utilisateurId) {
        return ResponseEntity.ok(notificationService.listerNonLues(utilisateurId));
    }

    @PatchMapping("/{id}/lire")
    public ResponseEntity<NotificationResponse> marquerCommeLue(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marquerCommeLue(id));
    }

    @PatchMapping("/{utilisateurId}/lire-tout")
    public ResponseEntity<Void> marquerToutesCommeLues(@PathVariable Long utilisateurId) {
        notificationService.marquerToutesCommeLues(utilisateurId);
        return ResponseEntity.noContent().build();
    }
}
