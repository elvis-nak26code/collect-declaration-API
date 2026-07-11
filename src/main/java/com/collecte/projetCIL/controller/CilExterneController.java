package com.collecte.projetCIL.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.collecte.projetCIL.dto.request.PlainteRequest;
import com.collecte.projetCIL.dto.response.DeclarationResponse;
import com.collecte.projetCIL.dto.response.HistoriqueDeclarationResponse;
import com.collecte.projetCIL.dto.response.PlainteResponse;
import com.collecte.projetCIL.models.CleApiCil;
import com.collecte.projetCIL.service.DeclarationService;
import com.collecte.projetCIL.service.PlainteService;

import lombok.RequiredArgsConstructor;

/**
 * Routes destinées au système EXTERNE de la CIL — pas de login, pas de JWT,
 * AUCUNE fiche CIL en base. Authentification uniquement via le header
 * "X-API-KEY" (voir CilApiKeyAuthFilter). Le partenaire est automatiquement
 * résolu à partir de la clé et injecté via @AuthenticationPrincipal CleApiCil.
 *
 * GET  /api/cil-externe/declarations              → déclarations en attente de vérification
 * GET  /api/cil-externe/declarations/{id}          → détail d'une déclaration
 * PUT  /api/cil-externe/declarations/{id}/valider  → valider la conformité
 * PUT  /api/cil-externe/declarations/{id}/rejeter  → rejeter (motif obligatoire, body {"motif": "..."})
 * GET  /api/cil-externe/declarations/historique    → déclarations déjà traitées avec cette clé
 * POST /api/cil-externe/plaintes                   → envoyer une plainte au DPO
 * GET  /api/cil-externe/plaintes                   → plaintes déjà émises avec cette clé
 */
@RestController
@RequestMapping("/api/cil-externe")
@RequiredArgsConstructor
public class CilExterneController {

    private final DeclarationService declarationService;
    private final PlainteService plainteService;

    // ── Déclarations ─────────────────────────────────────────────────── //

    @GetMapping("/declarations")
    public ResponseEntity<List<DeclarationResponse>> declarationsEnAttente() {
        return ResponseEntity.ok(declarationService.listerPourCil());
    }

    @GetMapping("/declarations/{id}")
    public ResponseEntity<DeclarationResponse> getDeclaration(@PathVariable Long id) {
        return ResponseEntity.ok(declarationService.getById(id));
    }

    @GetMapping("/declarations/historique")
    public ResponseEntity<List<HistoriqueDeclarationResponse>> historique(@AuthenticationPrincipal CleApiCil cle) {
        return ResponseEntity.ok(declarationService.listerHistoriqueParCleApi(cle.getId()));
    }

    @PutMapping("/declarations/{id}/valider")
    public ResponseEntity<DeclarationResponse> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal CleApiCil cle) {
        return ResponseEntity.ok(declarationService.validerConformiteCilExterne(id, cle));
    }

    @PutMapping("/declarations/{id}/rejeter")
    public ResponseEntity<?> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CleApiCil cle) {
        String motif = body.get("motif");
        if (motif == null || motif.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Le motif de rejet est obligatoire."));
        }
        return ResponseEntity.ok(declarationService.rejeterConformiteCilExterne(id, cle, motif));
    }

    // ── Plaintes ─────────────────────────────────────────────────────── //

    @PostMapping("/plaintes")
    public ResponseEntity<PlainteResponse> envoyerPlainte(
            @RequestBody PlainteRequest request,
            @AuthenticationPrincipal CleApiCil cle) {
        return ResponseEntity.ok(plainteService.envoyerPlainteCilVersDpoExterne(request, cle));
    }

    @GetMapping("/plaintes")
    public ResponseEntity<List<PlainteResponse>> mesPlaintes(@AuthenticationPrincipal CleApiCil cle) {
        return ResponseEntity.ok(plainteService.listerParCleApi(cle.getId()));
    }
}
