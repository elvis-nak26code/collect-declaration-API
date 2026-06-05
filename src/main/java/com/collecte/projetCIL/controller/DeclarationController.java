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
 * Routes déclarations.
 *
 * POST   /api/declarations/normale                 → créer déclaration normale      (DPO)
 * POST   /api/declarations/collecte-site           → créer déclaration site web     (DPO)
 * POST   /api/declarations/video-surveillance      → créer déclaration vidéo        (DPO)
 * POST   /api/declarations/autorisation            → créer déclaration autorisation  (DPO)
 *
 * GET    /api/declarations/mes-declarations        → mes déclarations               (DPO)
 * GET    /api/declarations/en-attente              → déclarations EN_ATTENTE        (DG)
 * GET    /api/declarations/{id}                    → détail d'une déclaration       (DPO, DG, CIL)
 *
 * PUT    /api/declarations/{id}/valider            → approuver                      (DG)
 * PUT    /api/declarations/{id}/rejeter            → rejeter + commentaire          (DG)
 */
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService declarationService;

    // ================================================================== //
    //  CRÉATION — 4 types
    // ================================================================== //

    @PostMapping("/normale")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<DeclarationResponse> creerNormale(
            @RequestBody DeclarationNormaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationNormale(request, userDetails.getUsername()));
    }

    @PostMapping("/collecte-site")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<DeclarationResponse> creerCollecteSite(
            @RequestBody DeclarationCollecteSiteInternetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationCollecteSite(request, userDetails.getUsername()));
    }

    @PostMapping("/video-surveillance")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<DeclarationResponse> creerVideoSurveillance(
            @RequestBody DeclarationVideoSurveillanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationVideoSurveillance(request, userDetails.getUsername()));
    }

    @PostMapping("/autorisation")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<DeclarationResponse> creerAutorisation(
            @RequestBody DeclarationAutorisationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.creerDeclarationAutorisation(request, userDetails.getUsername()));
    }

    // ================================================================== //
    //  CONSULTATION
    // ================================================================== //

    /** Le DPO voit ses propres déclarations — l'ID est déduit du token. */
    @GetMapping("/mes-declarations")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<List<DeclarationResponse>> mesDeclarations(
            @RequestParam Long dpoId) {
        return ResponseEntity.ok(declarationService.listerParDpo(dpoId));
    }

    /** La DG voit toutes les déclarations EN_ATTENTE de validation. */
    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyRole('DG', 'ADMINISTRATEUR')")
    public ResponseEntity<List<DeclarationResponse>> declarationsEnAttente() {
        return ResponseEntity.ok(declarationService.listerEnAttente());
    }

    /** Détail d'une déclaration — accessible au DPO, DG et CIL. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DPO', 'DG', 'CIL', 'ADMINISTRATEUR')")
    public ResponseEntity<DeclarationResponse> getDeclaration(@PathVariable Long id) {
        return ResponseEntity.ok(declarationService.getById(id));
    }

    // ================================================================== //
    //  VALIDATION / REJET PAR LA DG
    // ================================================================== //

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('DG')")
    public ResponseEntity<DeclarationResponse> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(declarationService.validerDeclaration(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('DG')")
    public ResponseEntity<DeclarationResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String commentaire = body.getOrDefault("commentaire", "Aucun commentaire fourni.");
        return ResponseEntity.ok(declarationService.rejeterDeclaration(id, userDetails.getUsername(), commentaire));
    }
}
