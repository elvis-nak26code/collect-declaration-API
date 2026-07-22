package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.SessionCollecteRequest;
import com.collecte.projetCIL.dto.response.SessionCollecteResponse;
import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.service.SessionCollecteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionCollecteController {

    private final SessionCollecteService sessionCollecteService;

    // POST /api/sessions
    // Créer une nouvelle session de collecte (réservé DPO ou ADMIN)
    @PostMapping
    // @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<SessionCollecteResponse> creerSession(
            @RequestBody SessionCollecteRequest request) {
        return ResponseEntity.ok(sessionCollecteService.creerSession(request));
    }

    // GET /api/sessions
    // Lister toutes les sessions
    @GetMapping
    public ResponseEntity<List<SessionCollecteResponse>> listerSessions() {
        return ResponseEntity.ok(sessionCollecteService.listerSessions());
    }

    // GET /api/sessions/{id}
    // Obtenir une session par ID
    @GetMapping("/{id}")
    public ResponseEntity<SessionCollecteResponse> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionCollecteService.getSessionById(id));
    }

    // PATCH /api/sessions/{id}/statut?valeur=TERMINEE
    // Changer le statut d'une session
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<SessionCollecteResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutSession valeur) {
        return ResponseEntity.ok(sessionCollecteService.changerStatut(id, valeur));
    }

    // PUT /api/sessions/{id}
    // Modifier une session existante (nom, lieu, type, description, dates).
    // NOTE : cet endpoint manquait — c'était la cause de l'erreur interne
    // renvoyée par le frontend lors de la modification d'une session
    // (le service SessionCollecteService.modifierSession() existait déjà
    // mais n'était jamais appelé, faute de route PUT).
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<SessionCollecteResponse> modifierSession(
            @PathVariable Long id,
            @RequestBody SessionCollecteRequest request) {
        return ResponseEntity.ok(sessionCollecteService.modifierSession(id, request));
    }
}