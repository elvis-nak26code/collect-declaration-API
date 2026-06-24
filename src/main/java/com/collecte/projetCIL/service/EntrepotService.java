package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.models.DonneePersonnelle;
import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.TypeDonnee;
import com.collecte.projetCIL.repository.DonneePersonnelleRepository;
import com.collecte.projetCIL.repository.PersonneRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.TypeDonneeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service gérant l'entrepôt de données personnelles.
 *
 * Les données de l'entrepôt sont des DonneePersonnelle dont le champ
 * traitement est NULL. Elles peuvent ensuite être "attachées" à un traitement
 * lors de la saisie via l'onglet "Depuis l'entrepôt".
 *
 * Format du fichier Excel (colonne A→F, ligne 1 ignorée car c'est l'en-tête) :
 *   A : nom          (obligatoire)
 *   B : prenom       (obligatoire)
 *   C : email        (optionnel)
 *   D : telephone    (optionnel)
 *   E : type_donnee  (nom exact ou approché, obligatoire)
 *   F : valeur       (obligatoire)
 */
@Service
@RequiredArgsConstructor
public class EntrepotService {

    private final DonneePersonnelleRepository donneePersonnelleRepository;
    private final PersonneRepository personneRepository;
    private final TypeDonneeRepository typeDonneeRepository;
    private final TraitementRepository traitementRepository;
    private final TraitementService traitementService;

    // ─── Import Excel ─────────────────────────────────────────────────────────

    public ImportResultResponse importerDepuisExcel(MultipartFile fichier) throws IOException {

        List<String> erreurs = new ArrayList<>();
        int totalLignes = 0;
        int lignesImportees = 0;

        // Cache des types de données (chargé une seule fois pour la session d'import)
        List<TypeDonnee> tousLesTypes = typeDonneeRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(fichier.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || estLigneVide(row)) continue;

                totalLignes++;
                try {
                    // Colonnes
                    String nom       = getCellString(row.getCell(0));
                    String prenom    = getCellString(row.getCell(1));
                    String email     = getCellString(row.getCell(2));
                    String telephone = getCellString(row.getCell(3));
                    String typeNom   = getCellString(row.getCell(4));
                    String valeur    = getCellString(row.getCell(5));

                    // Validation des champs obligatoires
                    if (nom == null || nom.isBlank())
                        throw new Exception("Colonne A (nom) vide");
                    if (prenom == null || prenom.isBlank())
                        throw new Exception("Colonne B (prenom) vide");
                    if (typeNom == null || typeNom.isBlank())
                        throw new Exception("Colonne E (type_donnee) vide");
                    if (valeur == null || valeur.isBlank())
                        throw new Exception("Colonne F (valeur) vide");

                    // Résolution du type de donnée par nom (insensible à la casse)
                    TypeDonnee typeDonnee = tousLesTypes.stream()
                            .filter(t -> t.getNom().equalsIgnoreCase(typeNom.trim()))
                            .findFirst()
                            .orElseThrow(() -> new Exception(
                                    "Type de donnée inconnu : \"" + typeNom + "\". "
                                    + "Types disponibles : " + tousLesTypes.stream()
                                            .map(TypeDonnee::getNom)
                                            .collect(Collectors.joining(", "))));

                    // Dédoublonnage personne par email puis téléphone
                    Personne personne = trouverOuCreerPersonne(nom.trim(), prenom.trim(),
                            email, telephone);

                    // Création de la donnée dans l'entrepôt (traitement = null)
                    DonneePersonnelle donnee = new DonneePersonnelle();
                    donnee.setValeur(valeur.trim());
                    donnee.setDateCollecte(LocalDateTime.now());
                    donnee.setPersonne(personne);
                    donnee.setTypeDonnee(typeDonnee);
                    donnee.setTraitement(null); // entrepôt : pas encore rattachée

                    donneePersonnelleRepository.save(donnee);
                    lignesImportees++;

                } catch (Exception e) {
                    erreurs.add("Ligne " + (i + 1) + " : " + e.getMessage());
                }
            }
        }

        return new ImportResultResponse(totalLignes, lignesImportees,
                totalLignes - lignesImportees, erreurs);
    }

    // ─── Listing ──────────────────────────────────────────────────────────────

    /**
     * Renvoie les données de l'entrepôt (traitement = null).
     * Si q est fourni, filtre par nom/prenom/valeur/type de donnée.
     */
    public List<DonneePersonnelleResponse> lister(String q) {
        List<DonneePersonnelle> entrepot = donneePersonnelleRepository.findEntrepot();
        if (q != null && !q.isBlank()) {
            String lq = q.toLowerCase().trim();
            entrepot = entrepot.stream()
                    .filter(d -> matches(d, lq))
                    .collect(Collectors.toList());
        }
        return entrepot.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private boolean matches(DonneePersonnelle d, String lq) {
        if (d.getValeur() != null && d.getValeur().toLowerCase().contains(lq)) return true;
        if (d.getPersonne() != null) {
            String nom = (d.getPersonne().getNom() + " " + d.getPersonne().getPrenom()).toLowerCase();
            if (nom.contains(lq)) return true;
        }
        if (d.getTypeDonnee() != null && d.getTypeDonnee().getNom().toLowerCase().contains(lq)) return true;
        return false;
    }

    // ─── Rattachement ─────────────────────────────────────────────────────────

    public DonneePersonnelleResponse attacher(Long donneeId, Long traitementId) {
        DonneePersonnelle donnee = donneePersonnelleRepository.findById(donneeId)
                .orElseThrow(() -> new RuntimeException("Donnée introuvable : " + donneeId));
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        donnee.setTraitement(traitement);
        DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);
        traitementService.incrementerNombreDonnee(traitementId, 1L);
        return toResponse(saved);
    }

    public List<DonneePersonnelleResponse> attacherLot(List<Long> donneeIds, Long traitementId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        List<DonneePersonnelleResponse> resultats = new ArrayList<>();
        int count = 0;
        for (Long id : donneeIds) {
            DonneePersonnelle donnee = donneePersonnelleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Donnée introuvable : " + id));
            if (donnee.getTraitement() == null) { // n'attache que les données libre
                donnee.setTraitement(traitement);
                resultats.add(toResponse(donneePersonnelleRepository.save(donnee)));
                count++;
            }
        }
        if (count > 0) traitementService.incrementerNombreDonnee(traitementId, (long) count);
        return resultats;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Personne trouverOuCreerPersonne(String nom, String prenom,
                                             String email, String telephone) {
        // Dédoublonnage par email
        if (email != null && !email.isBlank()) {
            Optional<Personne> existant = personneRepository.findByEmail(email.trim());
            if (existant.isPresent()) return existant.get();
        }
        // Dédoublonnage par téléphone
        if (telephone != null && !telephone.isBlank()) {
            Optional<Personne> existant = personneRepository.findByTelephone(telephone.trim());
            if (existant.isPresent()) return existant.get();
        }
        // Création
        Personne p = new Personne();
        p.setNom(nom);
        p.setPrenom(prenom);
        p.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        p.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
        p.setDateCreation(LocalDateTime.now());
        p.setDateModification(LocalDateTime.now());
        return personneRepository.save(p);
    }

    private boolean estLigneVide(Row row) {
        for (int c = 0; c <= 5; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellString(cell);
                if (val != null && !val.isBlank()) return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                // Évite "70000000.0" pour les numéros de téléphone
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf((long) cell.getNumericCellValue()); }
            }
            default -> null;
        };
    }

    private DonneePersonnelleResponse toResponse(DonneePersonnelle d) {
        String nomPersonne = d.getPersonne() != null
                ? (d.getPersonne().getPrenom() + " " + d.getPersonne().getNom()).trim()
                : null;
        String nomUsager = d.getUsager() != null
                ? (d.getUsager().getPrenom() + " " + d.getUsager().getNom()).trim()
                : null;
        return new DonneePersonnelleResponse(
                d.getIdDonnee(),
                d.getValeur(),
                d.getDateCollecte(),
                d.getUsager()     != null ? d.getUsager().getId()                : null,
                nomUsager,
                d.getPersonne()   != null ? d.getPersonne().getId()              : null,
                nomPersonne,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getIdTypeDonnee()  : null,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getNom()           : null,
                d.getTypeDonnee() != null ? d.getTypeDonnee().getSensible()      : null,
                d.getTraitement() != null ? d.getTraitement().getIdTraitement()  : null,
                d.getTraitement() != null ? d.getTraitement().getNom()           : null
        );
    }
}