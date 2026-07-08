package com.collecte.projetCIL.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.request.DeclarationAutorisationRequest;
import com.collecte.projetCIL.dto.request.DeclarationCollecteSiteInternetRequest;
import com.collecte.projetCIL.dto.request.DeclarationNormaleRequest;
import com.collecte.projetCIL.dto.request.DeclarationVideoSurveillanceRequest;
import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.enums.StatutTraitement;
import com.collecte.projetCIL.service.TraitementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/traitements")
@RequiredArgsConstructor
public class TraitementController {

    private final TraitementService traitementService;

    // ------------------------------------------------------------------ //
    //  POST /api/traitements/normale
    //  Crée un traitement + DeclarationNormale (sessionCollecteId optionnel)
    // ------------------------------------------------------------------ //
    @PostMapping("/normale")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationNormale(
            @RequestPart("traitement") TraitementRequest traitementRequest,
            @RequestPart("declaration") DeclarationNormaleRequest declarationRequest) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationNormale(traitementRequest, declarationRequest));
    }

    // ------------------------------------------------------------------ //
    //  POST /api/traitements/collecte-site-internet
    //  Crée un traitement + DeclarationCollecteSiteInternet (sessionCollecteId optionnel)
    // ------------------------------------------------------------------ //
    @PostMapping("/collecte-site-internet")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationCollecteSite(
            @RequestPart("traitement") TraitementRequest traitementRequest,
            @RequestPart("declaration") DeclarationCollecteSiteInternetRequest declarationRequest) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationCollecteSite(traitementRequest, declarationRequest));
    }

    // ------------------------------------------------------------------ //
    //  POST /api/traitements/video-surveillance
    //  Crée un traitement + DeclarationSystemeVideoSurveillance (sessionCollecteId optionnel)
    // ------------------------------------------------------------------ //
    @PostMapping("/video-surveillance")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationVideoSurveillance(
            @RequestPart("traitement") TraitementRequest traitementRequest,
            @RequestPart("declaration") DeclarationVideoSurveillanceRequest declarationRequest) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationVideoSurveillance(traitementRequest, declarationRequest));
    }

    // ------------------------------------------------------------------ //
    //  POST /api/traitements/autorisation
    //  Crée un traitement + DeclarationAutorisation (sessionCollecteId optionnel)
    // ------------------------------------------------------------------ //
    @PostMapping("/autorisation")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationAutorisation(
            @RequestPart("traitement") TraitementRequest traitementRequest,
            @RequestPart("declaration") DeclarationAutorisationRequest declarationRequest) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationAutorisation(traitementRequest, declarationRequest));
    }

    // ------------------------------------------------------------------ //
    //  GET /api/traitements/session/{sessionId}
    // ------------------------------------------------------------------ //
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<TraitementResponse>> listerParSession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(traitementService.listerParSession(sessionId));
    }

    // ------------------------------------------------------------------ //
    //  GET /api/traitements/sans-session/{utilisateurMetierId}
    //  Traitements sans session, créés par cet UtilisateurMetier
    // ------------------------------------------------------------------ //
    @GetMapping("/sans-session/{utilisateurMetierId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerSansSession(
            @PathVariable Long utilisateurMetierId) {
        return ResponseEntity.ok(traitementService.listerSansSession(utilisateurMetierId));
    }

    // ------------------------------------------------------------------ //
    //  GET /api/traitements/utilisateur-metier/{utilisateurMetierId}
    //  Tous les traitements (avec ou sans session) d'un UtilisateurMetier
    // ------------------------------------------------------------------ //
    @GetMapping("/utilisateur-metier/{utilisateurMetierId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerParUtilisateurMetier(
            @PathVariable Long utilisateurMetierId) {
        return ResponseEntity.ok(traitementService.listerParUtilisateurMetier(utilisateurMetierId));
    }

    // ------------------------------------------------------------------ //
    //  GET /api/traitements/{id}
    // ------------------------------------------------------------------ //
    @GetMapping("/{id}")
    public ResponseEntity<TraitementResponse> getTraitement(@PathVariable Long id) {
        return ResponseEntity.ok(traitementService.getTraitementById(id));
    }

    // PATCH /api/traitements/{id}/statut
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutTraitement statut) {
        return ResponseEntity.ok(traitementService.updateStatut(id, statut));
    }

    // ------------------------------------------------------------------ //
    //  PATCH /api/traitements/{id}/lier-session?sessionId=...
    //  Lie un traitement existant (sans session ou pour le déplacer) à une session
    // ------------------------------------------------------------------ //
    @PatchMapping("/{id}/lier-session")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> lierSession(
            @PathVariable Long id,
            @RequestParam Long sessionId) {
        return ResponseEntity.ok(traitementService.lierSession(id, sessionId));
    }

    // ------------------------------------------------------------------ //
    //  PATCH /api/traitements/{id}/delier-session
    //  Retire la session d'un traitement (le rend orphelin)
    // ------------------------------------------------------------------ //
    @PatchMapping("/{id}/delier-session")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> delierSession(@PathVariable Long id) {
        return ResponseEntity.ok(traitementService.delierSession(id));
    }

    // ------------------------------------------------------------------ //
    //  PATCH /api/traitements/{id}/envoyer-dpo?dpoId=... (dpoId optionnel)
    //  L'UtilisateurMetier envoie le traitement (et sa déclaration) au DPO.
    //  Avant cet appel, le DPO ne voit pas ce traitement.
    // ------------------------------------------------------------------ //
    @PatchMapping("/{id}/envoyer-dpo")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> envoyerAuDpo(
            @PathVariable Long id,
            @RequestParam(required = false) Long dpoId) {
        return ResponseEntity.ok(traitementService.envoyerAuDpo(id, dpoId));
    }

    // ------------------------------------------------------------------ //
    //  PUT /api/traitements/{id}
    //  Mise à jour complète d'un traitement (champs métier)
    // ------------------------------------------------------------------ //
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> updateTraitement(
            @PathVariable Long id,
            @RequestBody TraitementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(traitementService.updateTraitement(id, request, userDetails.getUsername()));
    }

    // ------------------------------------------------------------------ //
    //  DELETE /api/traitements/{id}
    //  Supprime un traitement et sa déclaration associée
    // ------------------------------------------------------------------ //
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteTraitement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        traitementService.deleteTraitement(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // GET /api/traitements — tous les traitements envoyés au DPO (DPO / Admin)
    // Filtre optionnel ?declare=true|false pour distinguer déclarés / non déclarés
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerTous(
            @RequestParam(required = false) Boolean declare) {
        return ResponseEntity.ok(traitementService.listerTous(declare));
    }

    // GET /api/traitements/dpo/{dpoId} — traitements envoyés au DPO donné
    // Filtre optionnel ?declare=true|false pour distinguer déclarés / non déclarés
    @GetMapping("/dpo/{dpoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerParDpo(
            @PathVariable Long dpoId,
            @RequestParam(required = false) Boolean declare) {
        return ResponseEntity.ok(traitementService.listerParDpo(dpoId, declare));
    }

}