package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.service.TraitementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traitements")
@RequiredArgsConstructor
public class TraitementController {

    private final TraitementService traitementService;

    // POST /api/traitements
    // Créer un traitement rattaché à une session
    @PostMapping
    @PreAuthorize("hasAnyRole('DPO', 'UTILISATEUR_METIER', 'ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerTraitement(
            @RequestBody TraitementRequest request) {
        return ResponseEntity.ok(traitementService.creerTraitement(request));
    }

    // GET /api/traitements/session/{sessionId}
    // Lister les traitements d'une session
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<TraitementResponse>> listerParSession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(traitementService.listerParSession(sessionId));
    }

    // GET /api/traitements/{id}
    // Obtenir un traitement par ID
    @GetMapping("/{id}")
    public ResponseEntity<TraitementResponse> getTraitement(@PathVariable Long id) {
        return ResponseEntity.ok(traitementService.getTraitementById(id));
    }
}
