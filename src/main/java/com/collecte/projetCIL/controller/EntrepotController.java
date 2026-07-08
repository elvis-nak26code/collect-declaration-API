package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.EntrepotSaisieRequest;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.service.EntrepotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Entrepôt de données — données personnelles sans traitement assigné.
 *
 * Le fichier Excel attendu a le format suivant (ligne 1 = en-tête ignorée) :
 *   Colonne A : nom          (String, obligatoire)
 *   Colonne B : prenom       (String, obligatoire)
 *   Colonne C : email        (String, optionnel)
 *   Colonne D : telephone    (String, optionnel)
 *   Colonne E : type_donnee  (nom du type, String, obligatoire)
 *   Colonne F : valeur       (String, obligatoire)
 *
 * La personne est créée ou récupérée automatiquement par email/téléphone
 * (dédoublonnage). Le type de donnée est recherché par nom (insensible à la casse).
 * Aucun ID manuel n'est requis.
 */
@RestController
@RequestMapping("/api/entrepot")
@RequiredArgsConstructor
public class EntrepotController {

    private final EntrepotService entrepotService;

    /**
     * Import Excel vers l'entrepôt (sans traitement).
     * POST /api/entrepot/import-excel
     */
    @PostMapping("/import-excel")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<ImportResultResponse> importerExcel(
            @RequestParam("fichier") MultipartFile fichier) throws IOException {

        if (fichier.isEmpty())
            throw new RuntimeException("Le fichier envoyé est vide. Veuillez sélectionner un fichier Excel valide.");
        String filename = fichier.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx"))
            throw new RuntimeException("Format de fichier invalide : seuls les fichiers .xlsx sont acceptés.");

        return ResponseEntity.ok(entrepotService.importerDepuisExcel(fichier));
    }

    /**
     * Saisie manuelle d'une donnée directement dans l'entrepôt (sans Excel).
     * POST /api/entrepot/saisie-manuelle
     */
    @PostMapping("/saisie-manuelle")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DonneePersonnelleResponse> saisirManuellement(
            @RequestBody EntrepotSaisieRequest request) {
        return ResponseEntity.ok(entrepotService.saisirManuellement(request));
    }

    /**
     * Liste toutes les données de l'entrepôt (traitement_id IS NULL).
     * GET /api/entrepot
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> lister(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(entrepotService.lister(q));
    }

    /**
     * Rattache une donnée de l'entrepôt à un traitement.
     * POST /api/entrepot/{donneeId}/attacher?traitementId=...
     */
    @PostMapping("/{donneeId}/attacher")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DonneePersonnelleResponse> attacher(
            @PathVariable Long donneeId,
            @RequestParam Long traitementId) {
        return ResponseEntity.ok(entrepotService.attacher(donneeId, traitementId));
    }

    /**
     * Rattache plusieurs données en une fois.
     * POST /api/entrepot/attacher-lot?traitementId=...
     */
    @PostMapping("/attacher-lot")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> attacherLot(
            @RequestParam Long traitementId,
            @RequestBody List<Long> donneeIds) {
        return ResponseEntity.ok(entrepotService.attacherLot(donneeIds, traitementId));
    }
}