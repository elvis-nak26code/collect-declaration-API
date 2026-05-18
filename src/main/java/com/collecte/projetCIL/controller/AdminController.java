package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.service.DemandeAccesService;
import com.collecte.projetCIL.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DemandeAccesService demandeAccesService;
    private final UtilisateurService utilisateurService;

    // Voir toutes les demandes en attente
    @GetMapping("/demandes/en-attente")
    public ResponseEntity<List<DemandeAccesResponse>> demandesEnAttente() {
        return ResponseEntity.ok(demandeAccesService.listerEnAttente());
    }

    // Voir toutes les demandes (tous statuts)
    @GetMapping("/demandes")
    public ResponseEntity<List<DemandeAccesResponse>> toutesLesDemandes() {
        return ResponseEntity.ok(demandeAccesService.listerToutes());
    }

    // Valider une demande
    @PutMapping("/demandes/{id}/valider")
    public ResponseEntity<MessageResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(demandeAccesService.valider(id));
    }

    // Rejeter une demande avec motif
    @PutMapping("/demandes/{id}/rejeter")
    public ResponseEntity<MessageResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motif = body.getOrDefault("motif", "Aucun motif fourni");
        return ResponseEntity.ok(demandeAccesService.rejeter(id, motif));
    }

    // Lister tous les utilisateurs
    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurResponse>> tousLesUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }
}
