package com.collecte.projetCIL.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.collecte.projetCIL.dto.request.AdminMotDePasseRequest;
import com.collecte.projetCIL.dto.request.AdminProfilRequest;
import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.JournalAuditResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.service.AdminProfilService;
import com.collecte.projetCIL.service.CleApiCilService;
import com.collecte.projetCIL.service.DemandeAccesService;
import com.collecte.projetCIL.service.JournalAuditService;
import com.collecte.projetCIL.service.UtilisateurService;
import com.collecte.projetCIL.dto.response.CleApiCilResponse;

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
 *
 * ── Statistiques dashboard ────────────────────────────────────────────
 * GET    /api/admin/stats                         → statistiques pour le camembert
 *
 * ── Profil admin ──────────────────────────────────────────────────────
 * PUT    /api/admin/profil                        → modifier nom/prénom
 * PUT    /api/admin/mot-de-passe                  → changer le mot de passe
 *
 * ── Clés API CIL (accès externe sans login) ───────────────────────────
 * POST   /api/admin/cil-externe/cle-api                    → créer une clé (fiche CIL auto-créée), affichée UNE fois
 * GET    /api/admin/cil-externe/cles                        → lister toutes les clés (sans la clé en clair)
 * POST   /api/admin/cil-externe/cle-api/{cleId}/regenerer   → régénérer la clé d'un partenaire existant
 * DELETE /api/admin/cil-externe/cle-api/{cleId}             → révoquer une clé
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMINISTRATEUR')")
public class AdminController {

    private final DemandeAccesService  demandeAccesService;
    private final UtilisateurService   utilisateurService;
    private final JournalAuditService  journalAuditService;
    private final AdminProfilService   adminProfilService;
    private final CleApiCilService     cleApiCilService;

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

    // ================================================================== //
    //  STATISTIQUES DASHBOARD (camembert)
    // ================================================================== //

    /**
     * Retourne des statistiques agrégées pour le camembert du dashboard :
     * - Répartition des utilisateurs par rôle
     * - Répartition des demandes par statut
     * - Répartition des actions du journal par module
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> statsTableauDeBord() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Répartition utilisateurs par type (hors Usagers)
        List<UtilisateurResponse> tousUtilisateurs = utilisateurService.listerTous();
        Map<String, Long> parRole = tousUtilisateurs.stream()
                .filter(u -> !"Usager".equals(u.getTypeUtilisateur()))
                .collect(Collectors.groupingBy(
                        u -> u.getTypeUtilisateur() != null ? u.getTypeUtilisateur() : "Inconnu",
                        Collectors.counting()
                ));
        stats.put("utilisateursParRole", parRole);

        // 2. Nombre d'usagers
        long nbUsagers = tousUtilisateurs.stream()
                .filter(u -> "Usager".equals(u.getTypeUtilisateur()))
                .count();
        stats.put("totalUsagers", nbUsagers);

        // 3. Répartition demandes par statut
        List<DemandeAccesResponse> toutesLesDemandes = demandeAccesService.listerToutes();
        Map<String, Long> demandesParStatut = toutesLesDemandes.stream()
                .filter(d -> d.getTypeUtilisateur() != null && !"Usager".equals(d.getTypeUtilisateur()))
                .collect(Collectors.groupingBy(
                        d -> d.getStatutDemandeAcces() != null ? d.getStatutDemandeAcces().name() : "INCONNU",
                        Collectors.counting()
                ));
        stats.put("demandesParStatut", demandesParStatut);

        // 4. Top modules dans le journal d'audit
        List<JournalAuditResponse> journaux = journalAuditService.listerTousTriesParDate();
        Map<String, Long> actionsParModule = journaux.stream()
                .filter(j -> j.getModuleConserne() != null)
                .collect(Collectors.groupingBy(
                        j -> j.getModuleConserne().name(),
                        Collectors.counting()
                ));
        stats.put("actionsParModule", actionsParModule);

        // 5. Actions par résultat (SUCCES / ECHEC)
        Map<String, Long> actionsParResultat = journaux.stream()
                .filter(j -> j.getResultatAction() != null)
                .collect(Collectors.groupingBy(
                        j -> j.getResultatAction().name(),
                        Collectors.counting()
                ));
        stats.put("actionsParResultat", actionsParResultat);

        return ResponseEntity.ok(stats);
    }

    // ================================================================== //
    //  PROFIL ADMIN
    // ================================================================== //

    @PutMapping("/profil")
    public ResponseEntity<MessageResponse> modifierProfil(
            Authentication authentication,
            @RequestBody AdminProfilRequest req) {
        return ResponseEntity.ok(adminProfilService.modifierProfil(authentication.getName(), req));
    }

    @PutMapping("/mot-de-passe")
    public ResponseEntity<MessageResponse> changerMotDePasse(
            Authentication authentication,
            @RequestBody AdminMotDePasseRequest req) {
        return ResponseEntity.ok(adminProfilService.changerMotDePasse(authentication.getName(), req));
    }

    // ================================================================== //
    //  CLÉS API CIL (accès externe sans login, voir CilApiKeyAuthFilter)
    // ================================================================== //

    /**
     * Crée une nouvelle clé API prête à l'emploi. Aucune fiche CIL n'a
     * besoin d'exister au préalable — elle est créée automatiquement en
     * interne. La clé en clair n'est renvoyée qu'ICI, une seule fois —
     * ensuite irrécupérable (seule son empreinte est stockée).
     * À communiquer immédiatement à la CIL.
     */
    @PostMapping("/cil-externe/cle-api")
    public ResponseEntity<Map<String, String>> genererCleApi(@RequestBody(required = false) Map<String, String> body) {
        String libelle = body != null ? body.get("libelle") : null;
        String cleEnClair = cleApiCilService.genererCle(libelle);
        return ResponseEntity.ok(Map.of(
                "cle", cleEnClair,
                "avertissement", "Cette clé ne sera plus jamais affichée. Copiez-la et transmettez-la à la CIL maintenant."
        ));
    }

    /** Liste toutes les clés API CIL émises (sans jamais exposer la clé en clair). */
    @GetMapping("/cil-externe/cles")
    public ResponseEntity<List<CleApiCilResponse>> listerClesApi() {
        return ResponseEntity.ok(cleApiCilService.listerToutes());
    }

    /** Régénère la clé d'un partenaire déjà existant (nouvelle clé, ancienne désactivée). */
    @PostMapping("/cil-externe/cle-api/{cleId}/regenerer")
    public ResponseEntity<Map<String, String>> regenererCleApi(@PathVariable Long cleId) {
        String cleEnClair = cleApiCilService.regenererCle(cleId);
        return ResponseEntity.ok(Map.of(
                "cle", cleEnClair,
                "avertissement", "Cette clé ne sera plus jamais affichée. Copiez-la et transmettez-la à la CIL maintenant."
        ));
    }

    /** Révoque une clé (elle cesse immédiatement de fonctionner). */
    @DeleteMapping("/cil-externe/cle-api/{cleId}")
    public ResponseEntity<MessageResponse> revoquerCleApi(@PathVariable Long cleId) {
        cleApiCilService.revoquerCle(cleId);
        return ResponseEntity.ok(new MessageResponse("Clé API révoquée avec succès."));
    }
}