package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.PersonneRequest;
import com.collecte.projetCIL.dto.response.PersonneResponse;
import com.collecte.projetCIL.service.PersonneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor
public class PersonneController {

    private final PersonneService personneService;

    /**
     * GET /api/personnes?q=texte
     * Recherche par nom, prénom, email ou téléphone (utilisé pour l'autocomplétion
     * lors de la saisie manuelle des données). Sans paramètre q, renvoie les
     * dernières personnes connues (liste bornée).
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<PersonneResponse>> rechercher(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(personneService.rechercher(q));
    }

    /**
     * POST /api/personnes
     * Crée une nouvelle personne concernée (ou renvoie la fiche existante si
     * l'email/le téléphone correspond déjà à une personne connue).
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PersonneResponse> creer(@RequestBody PersonneRequest request) {
        return ResponseEntity.ok(personneService.creerDepuisRequest(request));
    }
}
