package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.DemandeRequest;
import com.collecte.projetCIL.dto.response.DemandeResponse;
import com.collecte.projetCIL.service.DemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * POST   /api/demandes                              → usager soumet une demande
 * GET    /api/demandes/par-usager?usagerId=         → demandes d'un usager
 * GET    /api/demandes/par-um?umId=                 → demandes reçues par UtilisateurMetier
 * GET    /api/demandes/en-attente?umId=             → demandes EN_COURS pour UtilisateurMetier
 * PUT    /api/demandes/{id}/accepter               → UtilisateurMetier accepte
 * PUT    /api/demandes/{id}/rejeter                → UtilisateurMetier rejette
 */
@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> soumettreDemande(
            @RequestBody DemandeRequest request) {
        return ResponseEntity.ok(demandeService.soumettreDemandeUsager(request));
    }

    @GetMapping("/par-usager")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> parUsager(@RequestParam Long usagerId) {
        return ResponseEntity.ok(demandeService.listerParUsager(usagerId));
    }

    @GetMapping("/par-um")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> parUm(@RequestParam Long umId) {
        return ResponseEntity.ok(demandeService.listerParUtilisateurMetier(umId));
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> enAttente(@RequestParam Long umId) {
        return ResponseEntity.ok(demandeService.listerEnAttentePourUm(umId));
    }

    @PutMapping("/{id}/accepter")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> accepter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(demandeService.accepterDemande(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String motif = body.getOrDefault("motifRejet", "Aucun motif fourni.");
        return ResponseEntity.ok(demandeService.rejeterDemande(id, userDetails.getUsername(), motif));
    }
}
