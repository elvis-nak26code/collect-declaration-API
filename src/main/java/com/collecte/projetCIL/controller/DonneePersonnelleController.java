package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.DonneePersonnelleRequest;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.service.DonneePersonnelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/donnees")
@RequiredArgsConstructor
public class DonneePersonnelleController {

    private final DonneePersonnelleService donneePersonnelleService;

    // POST /api/donnees
    // Ajouter une donnée personnelle par saisie manuelle
    @PostMapping
    public ResponseEntity<DonneePersonnelleResponse> ajouterDonnee(
            @RequestBody DonneePersonnelleRequest request) {
        return ResponseEntity.ok(donneePersonnelleService.ajouterDonnee(request));
    }

    // POST /api/donnees/import-excel?traitementId=1
    // Importer des données depuis un fichier Excel (.xlsx)
    @PostMapping("/import-excel")
    public ResponseEntity<ImportResultResponse> importerExcel(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("traitementId") Long traitementId) throws IOException {

        if (fichier.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String originalFilename = fichier.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(donneePersonnelleService.importerDepuisExcel(fichier, traitementId));
    }

    // GET /api/donnees
    // Lister toutes les données personnelles
    @GetMapping
    public ResponseEntity<List<DonneePersonnelleResponse>> listerDonnees() {
        return ResponseEntity.ok(donneePersonnelleService.listerDonnees());
    }
}
