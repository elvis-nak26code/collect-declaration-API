package com.collecte.projetCIL.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.service.DemandeAccesService;
import com.collecte.projetCIL.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

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

    // ── Gestion du statut ──────────────────────────────

    // Suspendre
    @PutMapping("/utilisateurs/{id}/suspendre")
    public ResponseEntity<MessageResponse> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.suspendre(id));
    }

    // Réactiver
    @PutMapping("/utilisateurs/{id}/reactiver")
    public ResponseEntity<MessageResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.reactiver(id));
    }

    // Désactiver
    @PutMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<MessageResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.desactiver(id));
    }

    // Suppression logique (reste en base, statut = SUPPRIME)
    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<MessageResponse> supprimer(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.supprimer(id));
    }

    // Changer le statut manuellement
    // Body JSON : { "statut": "ACTIF" }  (ACTIF | INACTIF | SUSPENDU | SUPPRIME)
    @PatchMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<MessageResponse> changerStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        StatutUtilisateur statut = StatutUtilisateur.valueOf(
                body.getOrDefault("statut", "").toUpperCase()
        );
        return ResponseEntity.ok(utilisateurService.changerStatut(id, statut));
    }
}