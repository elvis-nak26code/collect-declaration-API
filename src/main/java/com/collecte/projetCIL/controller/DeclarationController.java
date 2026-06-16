package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.*;
import com.collecte.projetCIL.dto.response.DeclarationResponse;
import com.collecte.projetCIL.service.DeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ── Création (DPO) ────────────────────────────────────────────────────────
 * POST   /api/declarations/normale
 * POST   /api/declarations/collecte-site
 * POST   /api/declarations/video-surveillance
 * POST   /api/declarations/autorisation
 *
 * ── Consultation ──────────────────────────────────────────────────────────
 * GET    /api/declarations/mes-declarations?dpoId=     (DPO)
 * GET    /api/declarations/en-attente                   (DG)
 * GET    /api/declarations/pour-cil                     (CIL)
 * GET    /api/declarations/{id}                         (DPO, DG, CIL)
 *
 * ── Workflow DG ───────────────────────────────────────────────────────────
 * PUT    /api/declarations/{id}/valider                 (DG → transmet à CIL)
 * PUT    /api/declarations/{id}/rejeter                 (DG → renvoie au DPO)
 *
 * ── Workflow CIL ──────────────────────────────────────────────────────────
 * PUT    /api/declarations/{id}/valider-conformite      (CIL → conforme)
 * PUT    /api/declarations/{id}/rejeter-conformite      (CIL → non conforme)
 */
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService declarationService;

    // ── Création ──────────────────────────────────────────────────────── //

    @PostMapping("/normale")
    @PreAuthorize("hasAuthority('ROLE_DPO')")
    public ResponseEntity<DeclarationResponse> creerNormale(
            @RequestBody DeclarationNormaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationNormale(request, userDetails.getUsername()));
    }

    @PostMapping("/collecte-site")
    @PreAuthorize("hasAuthority('ROLE_DPO')")
    public ResponseEntity<DeclarationResponse> creerCollecteSite(
            @RequestBody DeclarationCollecteSiteInternetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationCollecteSite(request, userDetails.getUsername()));
    }

    @PostMapping("/video-surveillance")
    @PreAuthorize("hasAuthority('ROLE_DPO')")
    public ResponseEntity<DeclarationResponse> creerVideoSurveillance(
            @RequestBody DeclarationVideoSurveillanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationVideoSurveillance(request, userDetails.getUsername()));
    }

    @PostMapping("/autorisation")
    @PreAuthorize("hasAuthority('ROLE_DPO')")
    public ResponseEntity<DeclarationResponse> creerAutorisation(
            @RequestBody DeclarationAutorisationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationAutorisation(request, userDetails.getUsername()));
    }

    // ── Consultation ──────────────────────────────────────────────────── //

    @GetMapping("/mes-declarations")
    @PreAuthorize("hasAuthority('ROLE_DPO')")
    public ResponseEntity<List<DeclarationResponse>> mesDeclarations(@RequestParam Long dpoId) {
        return ResponseEntity.ok(declarationService.listerParDpo(dpoId));
    }

    /** DG : déclarations EN_ATTENTE soumises par le DPO. */
    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyAuthority('ROLE_DG','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DeclarationResponse>> declarationsEnAttente() {
        return ResponseEntity.ok(declarationService.listerEnAttente());
    }

    /** CIL : déclarations approuvées par la DG, en attente de vérification conformité. */
    @GetMapping("/pour-cil")
    @PreAuthorize("hasAnyAuthority('ROLE_CIL','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DeclarationResponse>> declarationsPourCil() {
        return ResponseEntity.ok(declarationService.listerPourCil());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_DG','ROLE_CIL','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DeclarationResponse> getDeclaration(@PathVariable Long id) {
        return ResponseEntity.ok(declarationService.getById(id));
    }

    // ── Workflow DG ───────────────────────────────────────────────────── //

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('ROLE_DG')")
    public ResponseEntity<DeclarationResponse> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.validerDeclaration(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAuthority('ROLE_DG')")
    public ResponseEntity<DeclarationResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String commentaire = body.getOrDefault("commentaire", "Aucun commentaire fourni.");
        return ResponseEntity.ok(declarationService.rejeterDeclaration(id, userDetails.getUsername(), commentaire));
    }

    // ── Workflow CIL ──────────────────────────────────────────────────── //

    @PutMapping("/{id}/valider-conformite")
    @PreAuthorize("hasAuthority('ROLE_CIL')")
    public ResponseEntity<DeclarationResponse> validerConformite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.validerConformiteCil(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/rejeter-conformite")
    @PreAuthorize("hasAuthority('ROLE_CIL')")
    public ResponseEntity<DeclarationResponse> rejeterConformite(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String commentaire = body.getOrDefault("commentaire", "Aucun commentaire fourni.");
        return ResponseEntity.ok(declarationService.rejeterConformiteCil(id, userDetails.getUsername(), commentaire));
    }
}
