package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.DonneePersonnelleRequest;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.models.DonneePersonnelle;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.TypeDonnee;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.repository.DonneePersonnelleRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.TypeDonneeRepository;
import com.collecte.projetCIL.repository.UsagerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonneePersonnelleService {

    private final DonneePersonnelleRepository donneePersonnelleRepository;
    private final UsagerRepository usagerRepository;
    private final TypeDonneeRepository typeDonneeRepository;
    private final TraitementRepository traitementRepository;
    private final TraitementService traitementService;

    // ------------------------------------------------------------------ //
    //  Ajouter une donnée par saisie manuelle
    // ------------------------------------------------------------------ //
    public DonneePersonnelleResponse ajouterDonnee(DonneePersonnelleRequest request) {

        Usager usager = usagerRepository.findById(request.getUsagerId())
                .orElseThrow(() -> new RuntimeException("Usager introuvable avec l'id : " + request.getUsagerId()));

        TypeDonnee typeDonnee = typeDonneeRepository.findById(request.getTypeDonneeId())
                .orElseThrow(() -> new RuntimeException("TypeDonnee introuvable avec l'id : " + request.getTypeDonneeId()));

        // Vérifier que le traitement existe
        traitementRepository.findById(request.getTraitementId())
                .orElseThrow(() -> new RuntimeException("Traitement introuvable avec l'id : " + request.getTraitementId()));

        DonneePersonnelle donnee = new DonneePersonnelle();
        donnee.setValeur(request.getValeur());
        donnee.setDateCollecte(
                request.getDateCollecte() != null ? request.getDateCollecte() : LocalDateTime.now()
        );
        donnee.setUsager(usager);
        donnee.setTypeDonnee(typeDonnee);

        DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);

        // Incrémenter le compteur du traitement
        traitementService.incrementerNombreDonnee(request.getTraitementId(), 1L);

        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  Ajouter des données via fichier Excel (.xlsx)
    //
    //  Format attendu du fichier Excel (colonnes dans cet ordre) :
    //  | valeur | dateCollecte (optionnel) | usagerId | typeDonneeId |
    // ------------------------------------------------------------------ //
    public ImportResultResponse importerDepuisExcel(MultipartFile fichier, Long traitementId) throws IOException {

        // Vérifier que le traitement existe
        traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable avec l'id : " + traitementId));

        List<String> erreurs = new ArrayList<>();
        int totalLignes = 0;
        int lignesImportees = 0;

        try (Workbook workbook = new XSSFWorkbook(fichier.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Ligne 0 = en-tête → on commence à 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalLignes++;
                try {
                    // Colonne 0 : valeur (String)
                    String valeur = getCellStringValue(row.getCell(0));
                    if (valeur == null || valeur.isBlank()) {
                        throw new Exception("La colonne 'valeur' est vide");
                    }

                    // Colonne 1 : dateCollecte (optionnel, peut être vide)
                    LocalDateTime dateCollecte = LocalDateTime.now();
                    Cell cellDate = row.getCell(1);
                    if (cellDate != null && cellDate.getCellType() != CellType.BLANK) {
                        try {
                            dateCollecte = cellDate.getLocalDateTimeCellValue();
                        } catch (Exception e) {
                            // Si la cellule n'est pas une date valide, on garde la date courante
                        }
                    }

                    // Colonne 2 : usagerId (numérique)
                    Long usagerId = getCellLongValue(row.getCell(2));
                    if (usagerId == null) throw new Exception("usagerId manquant ou invalide");

                    // Colonne 3 : typeDonneeId (numérique)
                    Long typeDonneeId = getCellLongValue(row.getCell(3));
                    if (typeDonneeId == null) throw new Exception("typeDonneeId manquant ou invalide");

                    // Résolution des entités
                    Usager usager = usagerRepository.findById(usagerId)
                            .orElseThrow(() -> new RuntimeException("Usager introuvable id=" + usagerId));

                    TypeDonnee typeDonnee = typeDonneeRepository.findById(typeDonneeId)
                            .orElseThrow(() -> new RuntimeException("TypeDonnee introuvable id=" + typeDonneeId));

                    // Création de la donnée
                    DonneePersonnelle donnee = new DonneePersonnelle();
                    donnee.setValeur(valeur);
                    donnee.setDateCollecte(dateCollecte);
                    donnee.setUsager(usager);
                    donnee.setTypeDonnee(typeDonnee);

                    donneePersonnelleRepository.save(donnee);
                    lignesImportees++;

                } catch (Exception e) {
                    erreurs.add("Ligne " + (i + 1) + " : " + e.getMessage());
                }
            }
        }

        // Incrémenter le compteur du traitement en une seule fois
        if (lignesImportees > 0) {
            traitementService.incrementerNombreDonnee(traitementId, lignesImportees);
        }

        int lignesEchouees = totalLignes - lignesImportees;
        return new ImportResultResponse(totalLignes, lignesImportees, lignesEchouees, erreurs);
    }

    // ------------------------------------------------------------------ //
    //  Lister les données d'un traitement (via jointure sur type/usager)
    // ------------------------------------------------------------------ //
    public List<DonneePersonnelleResponse> listerDonnees() {
        return donneePersonnelleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Helpers pour lire les cellules Excel
    // ------------------------------------------------------------------ //
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING -> Long.parseLong(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private DonneePersonnelleResponse toResponse(DonneePersonnelle d) {
        String nomUsager = (d.getUsager() != null)
                ? d.getUsager().getPrenom() + " " + d.getUsager().getNom()
                : null;
        return new DonneePersonnelleResponse(
                d.getIdDonnee(),
                d.getValeur(),
                d.getDateCollecte(),
                d.getUsager() != null ? d.getUsager().getId() : null,
                nomUsager,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getIdTypeDonnee() : null,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getNom() : null,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getSensible() : null
        );
    }
}
