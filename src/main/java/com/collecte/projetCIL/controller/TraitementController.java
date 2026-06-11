package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.DeclarationAutorisationRequest;
import com.collecte.projetCIL.dto.request.DeclarationCollecteSiteInternetRequest;
import com.collecte.projetCIL.dto.request.DeclarationNormaleRequest;
import com.collecte.projetCIL.dto.request.DeclarationVideoSurveillanceRequest;
import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.service.TraitementService;
import lombok.Data;
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

    // Wrappers internes (classes statiques) pour recevoir les deux objets en JSON pur
    @Data static class NormaleBody       { TraitementRequest traitement; DeclarationNormaleRequest declaration; }
    @Data static class CollecteSiteBody  { TraitementRequest traitement; DeclarationCollecteSiteInternetRequest declaration; }
    @Data static class VideoBody         { TraitementRequest traitement; DeclarationVideoSurveillanceRequest declaration; }
    @Data static class AutorisationBody  { TraitementRequest traitement; DeclarationAutorisationRequest declaration; }

    // POST /api/traitements/normale
    @PostMapping("/normale")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationNormale(@RequestBody NormaleBody body) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationNormale(body.getTraitement(), body.getDeclaration()));
    }

    // POST /api/traitements/collecte-site-internet
    @PostMapping("/collecte-site-internet")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationCollecteSite(@RequestBody CollecteSiteBody body) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationCollecteSite(body.getTraitement(), body.getDeclaration()));
    }

    // POST /api/traitements/video-surveillance
    @PostMapping("/video-surveillance")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationVideoSurveillance(@RequestBody VideoBody body) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationVideoSurveillance(body.getTraitement(), body.getDeclaration()));
    }

    // POST /api/traitements/autorisation
    @PostMapping("/autorisation")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TraitementResponse> creerAvecDeclarationAutorisation(@RequestBody AutorisationBody body) {
        return ResponseEntity.ok(
                traitementService.creerAvecDeclarationAutorisation(body.getTraitement(), body.getDeclaration()));
    }

    // GET /api/traitements/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<TraitementResponse>> listerParSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(traitementService.listerParSession(sessionId));
    }

    // GET /api/traitements/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TraitementResponse> getTraitement(@PathVariable Long id) {
        return ResponseEntity.ok(traitementService.getTraitementById(id));
    }
}