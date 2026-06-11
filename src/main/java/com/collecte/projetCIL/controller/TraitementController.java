package com.collecte.projetCIL.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    //  Crée un traitement + DeclarationNormale
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
    //  Crée un traitement + DeclarationCollecteSiteInternet
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
    //  Crée un traitement + DeclarationSystemeVideoSurveillance
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
    //  Crée un traitement + DeclarationAutorisation
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


    // GET /api/traitements — tous les traitements (DPO / Admin)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerTous() {
        return ResponseEntity.ok(traitementService.listerTous());
    }

    // GET /api/traitements/dpo/{dpoId} — traitements des sessions du DPO
    @GetMapping("/dpo/{dpoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TraitementResponse>> listerParDpo(@PathVariable Long dpoId) {
        return ResponseEntity.ok(traitementService.listerParDpo(dpoId));
    }

}