package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.DonneePersonnelleRequest;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.service.DonneePersonnelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/donnees")
@RequiredArgsConstructor
public class DonneePersonnelleController {

    private final DonneePersonnelleService donneePersonnelleService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DonneePersonnelleResponse> ajouterDonnee(
            @RequestBody DonneePersonnelleRequest request) {
        return ResponseEntity.ok(donneePersonnelleService.ajouterDonnee(request));
    }

    @PostMapping("/import-excel")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<ImportResultResponse> importerExcel(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("traitementId") Long traitementId) throws IOException {

        if (fichier.isEmpty()) return ResponseEntity.badRequest().build();
        String filename = fichier.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(donneePersonnelleService.importerDepuisExcel(fichier, traitementId));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> listerDonnees() {
        return ResponseEntity.ok(donneePersonnelleService.listerDonnees());
    }

    @GetMapping("/par-usager")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> listerParUsager(
            @RequestParam Long usagerId) {
        return ResponseEntity.ok(donneePersonnelleService.listerParUsager(usagerId));
    }

    @GetMapping("/par-personne")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> listerParPersonne(
            @RequestParam Long personneId) {
        return ResponseEntity.ok(donneePersonnelleService.listerParPersonne(personneId));
    }

    @GetMapping("/par-traitement")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> listerParTraitement(
            @RequestParam Long traitementId) {
        return ResponseEntity.ok(donneePersonnelleService.listerParTraitement(traitementId));
    }
}
