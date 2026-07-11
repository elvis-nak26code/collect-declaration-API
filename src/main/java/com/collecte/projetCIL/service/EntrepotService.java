package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.EntrepotSaisieRequest;
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
 * Formats acceptés (ligne 1 ignorée = en-tête) :
 *   Format A (standard) : NOM | PRENOM | EMAIL | TELEPHONE | DATE_NAISSANCE | NUMERO_CNIB | PROFESSION
 *   Format B (explicite) : nom | prenom | email | telephone | type_donnee | valeur
 *
 * Les doublons (même personne + même type + même valeur) sont rejetés silencieusement.
 */
@Service
@RequiredArgsConstructor
public class EntrepotService {

    private final DonneePersonnelleRepository donneePersonnelleRepository;
    private final PersonneRepository personneRepository;
    private final TypeDonneeRepository typeDonneeRepository;
    private final TraitementRepository traitementRepository;
    private final TraitementService traitementService;
    private final CollecteNotificationService collecteNotificationService;

    // ─── Saisie manuelle ──────────────────────────────────────────────────────

    /**
     * Ajoute une donnée directement dans l'entrepôt (traitement = null),
     * via saisie manuelle (formulaire), sans passer par un fichier Excel.
     * Même logique anti-doublon et de dédoublonnage de personne que l'import.
     */
    public DonneePersonnelleResponse saisirManuellement(EntrepotSaisieRequest request) {

        if (request.getNom() == null || request.getNom().isBlank())
            throw new RuntimeException("Le nom est obligatoire.");
        if (request.getPrenom() == null || request.getPrenom().isBlank())
            throw new RuntimeException("Le prénom est obligatoire.");
        if (request.getTypeDonneeId() == null)
            throw new RuntimeException("Le type de donnée est obligatoire.");
        if (request.getValeur() == null || request.getValeur().isBlank())
            throw new RuntimeException("La valeur est obligatoire.");

        TypeDonnee typeDonnee = typeDonneeRepository.findById(request.getTypeDonneeId())
                .orElseThrow(() -> new RuntimeException("TypeDonnee introuvable : " + request.getTypeDonneeId()));

        Personne personne = trouverOuCreerPersonne(
                request.getNom().trim(), request.getPrenom().trim(),
                request.getEmail(), request.getTelephone());

        List<DonneePersonnelle> entrepotActuel = donneePersonnelleRepository.findEntrepot();
        String valeur = request.getValeur().trim();

        if (estDoublonEntrepot(personne, typeDonnee, valeur, entrepotActuel)) {
            throw new RuntimeException(
                    "Doublon détecté : cette donnée (" + typeDonnee.getNom() + " = " + valeur
                    + ") existe déjà dans l'entrepôt pour cette personne.");
        }

        DonneePersonnelle donnee = new DonneePersonnelle();
        donnee.setValeur(valeur);
        donnee.setDateCollecte(LocalDateTime.now());
        donnee.setPersonne(personne);
        donnee.setTypeDonnee(typeDonnee);
        donnee.setTraitement(null);

        DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);
        collecteNotificationService.notifierCollecte(personne);
        return toResponse(saved);
    }

    // ─── Import Excel ─────────────────────────────────────────────────────────

    public ImportResultResponse importerDepuisExcel(MultipartFile fichier) throws IOException {

        List<String> erreurs = new ArrayList<>();
        int totalLignes = 0;
        int lignesImportees = 0;

        List<TypeDonnee> tousLesTypes = typeDonneeRepository.findAll();
        // Cache de l'entrepôt actuel pour accélérer les vérifications de doublons
        List<DonneePersonnelle> entrepotActuel = donneePersonnelleRepository.findEntrepot();

        try (Workbook workbook = new XSSFWorkbook(fichier.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            // Lire les en-têtes pour détecter le format
            Row headerRow = sheet.getRow(0);
            java.util.Map<String, Integer> colIndex = new java.util.HashMap<>();
            if (headerRow != null) {
                for (int c = 0; c <= headerRow.getLastCellNum(); c++) {
                    Cell cell = headerRow.getCell(c);
                    if (cell != null) {
                        String header = getCellString(cell);
                        if (header != null) colIndex.put(header.toLowerCase().trim(), c);
                    }
                }
            }

            // Format A si les colonnes standard sont présentes (NOM/PRENOM/EMAIL/TELEPHONE/...)
            boolean isFormatA = colIndex.containsKey("nom") && colIndex.containsKey("prenom")
                    && !colIndex.containsKey("type_donnee");

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || estLigneVide(row)) continue;

                totalLignes++;
                try {
                    if (isFormatA) {
                        // ── Format A : NOM | PRENOM | EMAIL | TELEPHONE | DATE_NAISSANCE | NUMERO_CNIB | PROFESSION ──
                        int iNom        = colIndex.getOrDefault("nom", 0);
                        int iPrenom     = colIndex.getOrDefault("prenom", 1);
                        int iEmail      = colIndex.getOrDefault("email", 2);
                        int iTel        = colIndex.getOrDefault("telephone", 3);
                        int iNaissance  = colIndex.getOrDefault("date_naissance", 4);
                        int iCnib       = colIndex.getOrDefault("numero_cnib", 5);
                        int iProfession = colIndex.getOrDefault("profession", 6);

                        String nom          = getCellString(row.getCell(iNom));
                        String prenom       = getCellString(row.getCell(iPrenom));
                        String email        = getCellString(row.getCell(iEmail));
                        String telephone    = getCellString(row.getCell(iTel));
                        String dateNaissance= getCellString(row.getCell(iNaissance));
                        String cnib         = getCellString(row.getCell(iCnib));
                        String profession   = getCellString(row.getCell(iProfession));

                        if (nom == null || nom.isBlank()) throw new Exception("Colonne NOM vide");
                        if (prenom == null || prenom.isBlank()) throw new Exception("Colonne PRENOM vide");

                        Personne personne = trouverOuCreerPersonne(nom.trim(), prenom.trim(), email, telephone);

                        int count = 0;
                        if (telephone != null && !telephone.isBlank())
                            count += sauvegarderEntrepot(personne, tousLesTypes, "Téléphone", telephone.trim(), entrepotActuel);
                        if (email != null && !email.isBlank())
                            count += sauvegarderEntrepot(personne, tousLesTypes, "Email", email.trim(), entrepotActuel);
                        if (dateNaissance != null && !dateNaissance.isBlank())
                            count += sauvegarderEntrepot(personne, tousLesTypes, "Date de naissance", dateNaissance.trim(), entrepotActuel);
                        if (cnib != null && !cnib.isBlank())
                            count += sauvegarderEntrepot(personne, tousLesTypes, "Numéro CNIB", cnib.trim(), entrepotActuel);
                        if (profession != null && !profession.isBlank())
                            count += sauvegarderEntrepot(personne, tousLesTypes, "Profession", profession.trim(), entrepotActuel);

                        if (count == 0) throw new Exception("Aucune donnée valide (doublons exclus ou champs vides)");
                        lignesImportees += count;
                        collecteNotificationService.notifierCollecte(personne);

                    } else {
                        // ── Format B : nom | prenom | email | telephone | type_donnee | valeur ──
                        int iNom    = colIndex.getOrDefault("nom", 0);
                        int iPrenom = colIndex.getOrDefault("prenom", 1);
                        int iEmail  = colIndex.getOrDefault("email", 2);
                        int iTel    = colIndex.getOrDefault("telephone", 3);
                        int iType   = colIndex.getOrDefault("type_donnee", 4);
                        int iValeur = colIndex.getOrDefault("valeur", 5);

                        String nom       = getCellString(row.getCell(iNom));
                        String prenom    = getCellString(row.getCell(iPrenom));
                        String email     = getCellString(row.getCell(iEmail));
                        String telephone = getCellString(row.getCell(iTel));
                        String typeNom   = getCellString(row.getCell(iType));
                        String valeur    = getCellString(row.getCell(iValeur));

                        if (nom == null || nom.isBlank())    throw new Exception("Colonne A (nom) vide");
                        if (prenom == null || prenom.isBlank()) throw new Exception("Colonne B (prenom) vide");
                        if (typeNom == null || typeNom.isBlank()) throw new Exception("Colonne E (type_donnee) vide");
                        if (valeur == null || valeur.isBlank())   throw new Exception("Colonne F (valeur) vide");

                        TypeDonnee typeDonnee = tousLesTypes.stream()
                                .filter(t -> t.getNom().equalsIgnoreCase(typeNom.trim()))
                                .findFirst()
                                .orElseThrow(() -> new Exception(
                                        "Type de donnée inconnu : \"" + typeNom + "\". "
                                        + "Types disponibles : " + tousLesTypes.stream()
                                                .map(TypeDonnee::getNom)
                                                .collect(Collectors.joining(", "))));

                        Personne personne = trouverOuCreerPersonne(nom.trim(), prenom.trim(), email, telephone);

                        // Anti-doublon dans l'entrepôt
                        if (estDoublonEntrepot(personne, typeDonnee, valeur.trim(), entrepotActuel)) {
                            erreurs.add("Ligne " + (i + 1) + " : doublon ignoré (" + typeNom + " = " + valeur + ")");
                        } else {
                            DonneePersonnelle donnee = new DonneePersonnelle();
                            donnee.setValeur(valeur.trim());
                            donnee.setDateCollecte(LocalDateTime.now());
                            donnee.setPersonne(personne);
                            donnee.setTypeDonnee(typeDonnee);
                            donnee.setTraitement(null);
                            DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);
                            entrepotActuel.add(saved); // mise à jour du cache local
                            lignesImportees++;
                            collecteNotificationService.notifierCollecte(personne);
                        }
                    }

                } catch (Exception e) {
                    erreurs.add("Ligne " + (i + 1) + " : " + e.getMessage());
                }
            }
        }

        return new ImportResultResponse(totalLignes, lignesImportees,
                totalLignes - lignesImportees, erreurs);
    }

    // ─── Listing ──────────────────────────────────────────────────────────────

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
        DonneePersonnelle donneeEntrepot = donneePersonnelleRepository.findById(donneeId)
                .orElseThrow(() -> new RuntimeException(
                        "Cette donnée n'existe pas (ou plus) dans l'entrepôt : " + donneeId));

        if (donneeEntrepot.getTraitement() != null) {
            throw new RuntimeException(
                    "Cette donnée n'est plus dans l'entrepôt : elle est déjà rattachée au traitement \""
                    + donneeEntrepot.getTraitement().getNom() + "\".");
        }

        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        if (estDejaDansTraitement(donneeEntrepot, traitementId)) {
            throw new RuntimeException(
                    "Cette donnée (" + nomType(donneeEntrepot) + " = " + donneeEntrepot.getValeur()
                    + ") est déjà rattachée au traitement \"" + traitement.getNom() + "\".");
        }

        // ── On COPIE la donnée dans le traitement : l'original reste dans
        //    l'entrepôt, disponible pour être rattaché à d'autres traitements.
        DonneePersonnelle copie = new DonneePersonnelle();
        copie.setValeur(donneeEntrepot.getValeur());
        copie.setDateCollecte(LocalDateTime.now());
        copie.setTypeDonnee(donneeEntrepot.getTypeDonnee());
        copie.setPersonne(donneeEntrepot.getPersonne());
        copie.setTraitement(traitement);

        DonneePersonnelle saved = donneePersonnelleRepository.save(copie);
        traitementService.incrementerNombreDonnee(traitementId, 1L);
        return toResponse(saved);
    }

    public List<DonneePersonnelleResponse> attacherLot(List<Long> donneeIds, Long traitementId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        List<DonneePersonnelleResponse> resultats = new ArrayList<>();
        List<String> ignorees = new ArrayList<>();
        int count = 0;

        for (Long id : donneeIds) {
            DonneePersonnelle donneeEntrepot = donneePersonnelleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Cette donnée n'existe pas (ou plus) dans l'entrepôt : " + id));

            // Une donnée déjà rattachée ailleurs, ou déjà présente dans CE
            // traitement, est simplement ignorée (pas d'erreur bloquante pour
            // le reste du lot), mais on le signale clairement dans le résultat.
            if (donneeEntrepot.getTraitement() != null) {
                ignorees.add(nomType(donneeEntrepot) + " = " + donneeEntrepot.getValeur()
                        + " (déjà rattachée à \"" + donneeEntrepot.getTraitement().getNom() + "\")");
                continue;
            }
            if (estDejaDansTraitement(donneeEntrepot, traitementId)) {
                ignorees.add(nomType(donneeEntrepot) + " = " + donneeEntrepot.getValeur()
                        + " (déjà présente dans ce traitement)");
                continue;
            }

            DonneePersonnelle copie = new DonneePersonnelle();
            copie.setValeur(donneeEntrepot.getValeur());
            copie.setDateCollecte(LocalDateTime.now());
            copie.setTypeDonnee(donneeEntrepot.getTypeDonnee());
            copie.setPersonne(donneeEntrepot.getPersonne());
            copie.setTraitement(traitement);

            resultats.add(toResponse(donneePersonnelleRepository.save(copie)));
            count++;
        }

        if (count > 0) traitementService.incrementerNombreDonnee(traitementId, (long) count);

        if (resultats.isEmpty() && !ignorees.isEmpty()) {
            throw new RuntimeException(
                    "Aucune donnée n'a pu être rattachée (toutes déjà présentes) : "
                    + String.join(" ; ", ignorees));
        }

        return resultats;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Sauvegarde une donnée dans l'entrepôt après vérification anti-doublon.
     * Met à jour le cache local entrepotActuel.
     */
    private int sauvegarderEntrepot(Personne personne, List<TypeDonnee> tousLesTypes,
            String typeNom, String valeur, List<DonneePersonnelle> entrepotActuel) {

        TypeDonnee type = tousLesTypes.stream()
                .filter(t -> t.getNom().equalsIgnoreCase(typeNom))
                .findFirst().orElse(null);
        if (type == null) return 0;

        if (estDoublonEntrepot(personne, type, valeur, entrepotActuel)) return 0;

        DonneePersonnelle donnee = new DonneePersonnelle();
        donnee.setValeur(valeur);
        donnee.setDateCollecte(LocalDateTime.now());
        donnee.setPersonne(personne);
        donnee.setTypeDonnee(type);
        donnee.setTraitement(null);
        DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);
        entrepotActuel.add(saved);
        return 1;
    }

    private boolean estDoublonEntrepot(Personne personne, TypeDonnee typeDonnee,
            String valeur, List<DonneePersonnelle> entrepotActuel) {
        if (personne == null || typeDonnee == null) return false;
        return entrepotActuel.stream().anyMatch(d ->
            d.getTypeDonnee() != null
            && d.getTypeDonnee().getIdTypeDonnee().equals(typeDonnee.getIdTypeDonnee())
            && d.getValeur() != null
            && d.getValeur().equalsIgnoreCase(valeur)
            && d.getPersonne() != null
            && d.getPersonne().getId().equals(personne.getId())
        );
    }

    private boolean estDejaDansTraitement(DonneePersonnelle donneeEntrepot, Long traitementId) {
        if (donneeEntrepot.getPersonne() == null || donneeEntrepot.getTypeDonnee() == null) return false;
        return donneePersonnelleRepository.findByTraitementId(traitementId).stream()
                .anyMatch(d ->
                    d.getTypeDonnee() != null
                    && d.getTypeDonnee().getIdTypeDonnee().equals(donneeEntrepot.getTypeDonnee().getIdTypeDonnee())
                    && d.getValeur() != null
                    && d.getValeur().equalsIgnoreCase(donneeEntrepot.getValeur())
                    && d.getPersonne() != null
                    && d.getPersonne().getId().equals(donneeEntrepot.getPersonne().getId())
                );
    }

    private String nomType(DonneePersonnelle d) {
        return d.getTypeDonnee() != null ? d.getTypeDonnee().getNom() : "Donnée";
    }

    private Personne trouverOuCreerPersonne(String nom, String prenom,
                                             String email, String telephone) {
        if (email != null && !email.isBlank()) {
            Optional<Personne> existant = personneRepository.findByEmail(email.trim());
            if (existant.isPresent()) return existant.get();
        }
        if (telephone != null && !telephone.isBlank()) {
            Optional<Personne> existant = personneRepository.findByTelephone(telephone.trim());
            if (existant.isPresent()) return existant.get();
        }
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
        for (int c = 0; c <= 6; c++) {
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