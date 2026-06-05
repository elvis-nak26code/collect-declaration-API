package com.collecte.projetCIL.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.JournalAuditResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.service.DemandeAccesService;
import com.collecte.projetCIL.service.JournalAuditService;
import com.collecte.projetCIL.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

/**
 * Routes admin.
 *
 * ── Demandes d'accès ──────────────────────────────────────────────────
 * GET    /api/admin/demandes/en-attente           → demandes en attente
 * GET    /api/admin/demandes                      → toutes les demandes
 * PUT    /api/admin/demandes/{id}/valider         → valider une demande
 * PUT    /api/admin/demandes/{id}/rejeter         → rejeter une demande
 *
 * ── Utilisateurs ──────────────────────────────────────────────────────
 * GET    /api/admin/utilisateurs                  → tous les utilisateurs
 * PUT    /api/admin/utilisateurs/{id}/statut      → changer le statut
 * DELETE /api/admin/utilisateurs/{id}             → supprimer
 *
 * ── Journal d'audit ───────────────────────────────────────────────────
 * GET    /api/admin/journal-audit                 → tous les journaux (anti-chron.)
 * GET    /api/admin/journal-audit/{utilisateurId} → journaux d'un utilisateur
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class AdminController {

    private final DemandeAccesService  demandeAccesService;
    private final UtilisateurService   utilisateurService;
    private final JournalAuditService  journalAuditService;

    // ================================================================== //
    //  DEMANDES D'ACCÈS
    // ================================================================== //

    @GetMapping("/demandes/en-attente")
    public ResponseEntity<List<DemandeAccesResponse>> demandesEnAttente() {
        return ResponseEntity.ok(demandeAccesService.listerEnAttente());
    }

    @GetMapping("/demandes")
    public ResponseEntity<List<DemandeAccesResponse>> toutesLesDemandes() {
        return ResponseEntity.ok(demandeAccesService.listerToutes());
    }

    @PutMapping("/demandes/{id}/valider")
    public ResponseEntity<MessageResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(demandeAccesService.valider(id));
    }

    @PutMapping("/demandes/{id}/rejeter")
    public ResponseEntity<MessageResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motif = body.getOrDefault("motif", "Aucun motif fourni");
        return ResponseEntity.ok(demandeAccesService.rejeter(id, motif));
    }

    // ================================================================== //
    //  UTILISATEURS
    // ================================================================== //

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurResponse>> tousLesUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    @PutMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<MessageResponse> changerStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        StatutUtilisateur statut = StatutUtilisateur.valueOf(body.get("statut"));
        return ResponseEntity.ok(utilisateurService.changerStatut(id, statut));
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<MessageResponse> supprimerUtilisateur(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.supprimer(id));
    }

    // ================================================================== //
    //  JOURNAL D'AUDIT
    // ================================================================== //

    @GetMapping("/journal-audit")
    public ResponseEntity<List<JournalAuditResponse>> tousLesJournaux() {
        return ResponseEntity.ok(journalAuditService.listerTousTriesParDate());
    }

    @GetMapping("/journal-audit/{utilisateurId}")
    public ResponseEntity<List<JournalAuditResponse>> journauxParUtilisateur(
            @PathVariable Long utilisateurId) {
        return ResponseEntity.ok(journalAuditService.listerParUtilisateur(utilisateurId));
    }
}