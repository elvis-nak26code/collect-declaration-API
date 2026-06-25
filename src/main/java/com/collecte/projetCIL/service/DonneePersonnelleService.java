package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.DonneePersonnelleRequest;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.dto.response.ImportResultResponse;
import com.collecte.projetCIL.models.DonneePersonnelle;
import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.TypeDonnee;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.repository.DonneePersonnelleRepository;
import com.collecte.projetCIL.repository.PersonneRepository;
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
    private final PersonneRepository personneRepository;
    private final TypeDonneeRepository typeDonneeRepository;
    private final TraitementRepository traitementRepository;
    private final TraitementService traitementService;

    // ─────────────────────────────────────────────────────────────────────────
    //  AJOUT MANUEL — copie aussi dans l'entrepôt + anti-doublon
    // ─────────────────────────────────────────────────────────────────────────
    public DonneePersonnelleResponse ajouterDonnee(DonneePersonnelleRequest request) {

        TypeDonnee typeDonnee = typeDonneeRepository.findById(request.getTypeDonneeId())
                .orElseThrow(() -> new RuntimeException("TypeDonnee introuvable : " + request.getTypeDonneeId()));

        Traitement traitement = traitementRepository.findById(request.getTraitementId())
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + request.getTraitementId()));

        Personne personne = null;
        Usager usager = null;

        if (request.getPersonneId() != null) {
            personne = personneRepository.findById(request.getPersonneId())
                    .orElseThrow(() -> new RuntimeException("Personne introuvable : " + request.getPersonneId()));
        } else if (request.getUsagerId() != null) {
            usager = usagerRepository.findById(request.getUsagerId())
                    .orElseThrow(() -> new RuntimeException("Usager introuvable : " + request.getUsagerId()));
            if (usager.getPersonne() != null) {
                personne = usager.getPersonne();
            }
        }

        // ── Anti-doublon dans le traitement ──────────────────────────────────
        // On refuse si la même (personne, typeDonnee, valeur) existe déjà dans ce traitement
        final Personne personneFinal = personne;
        boolean doublon = donneePersonnelleRepository.findByTraitementId(traitement.getIdTraitement())
                .stream()
                .anyMatch(d ->
                    d.getTypeDonnee() != null
                    && d.getTypeDonnee().getIdTypeDonnee().equals(typeDonnee.getIdTypeDonnee())
                    && d.getValeur() != null
                    && d.getValeur().equalsIgnoreCase(request.getValeur())
                    && d.getPersonne() != null
                    && personneFinal != null
                    && d.getPersonne().getId().equals(personneFinal.getId())
                );
        if (doublon) {
            throw new RuntimeException(
                "Doublon détecté : cette donnée (" + typeDonnee.getNom() + " = " + request.getValeur()
                + ") existe déjà pour cette personne dans ce traitement.");
        }

        // ── Enregistrement dans le traitement ────────────────────────────────
        DonneePersonnelle donnee = new DonneePersonnelle();
        donnee.setValeur(request.getValeur());
        donnee.setDateCollecte(request.getDateCollecte() != null ? request.getDateCollecte() : LocalDateTime.now());
        donnee.setTypeDonnee(typeDonnee);
        donnee.setTraitement(traitement);
        donnee.setPersonne(personne);
        donnee.setUsager(usager);

        DonneePersonnelle saved = donneePersonnelleRepository.save(donnee);
        traitementService.incrementerNombreDonnee(request.getTraitementId(), 1L);

        // ── Copie dans l'entrepôt (traitement = null) ────────────────────────
        // Uniquement si une personne est connue et si la valeur n'existe pas déjà dans l'entrepôt
        if (personne != null) {
            final Personne p = personne;
            boolean dejaEntrepot = donneePersonnelleRepository.findEntrepot()
                    .stream()
                    .anyMatch(d ->
                        d.getTypeDonnee() != null
                        && d.getTypeDonnee().getIdTypeDonnee().equals(typeDonnee.getIdTypeDonnee())
                        && d.getValeur() != null
                        && d.getValeur().equalsIgnoreCase(request.getValeur())
                        && d.getPersonne() != null
                        && d.getPersonne().getId().equals(p.getId())
                    );
            if (!dejaEntrepot) {
                DonneePersonnelle copieEntrepot = new DonneePersonnelle();
                copieEntrepot.setValeur(request.getValeur());
                copieEntrepot.setDateCollecte(LocalDateTime.now());
                copieEntrepot.setTypeDonnee(typeDonnee);
                copieEntrepot.setPersonne(personne);
                copieEntrepot.setUsager(usager);
                copieEntrepot.setTraitement(null); // entrepôt = pas de traitement
                donneePersonnelleRepository.save(copieEntrepot);
            }
        }

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  IMPORT EXCEL DIRECT DANS TRAITEMENT (avec anti-doublon)
    // ─────────────────────────────────────────────────────────────────────────
    public ImportResultResponse importerDepuisExcel(MultipartFile fichier, Long traitementId) throws IOException {

        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        List<String> erreurs = new ArrayList<>();
        int totalLignes = 0;
        int lignesImportees = 0;

        List<TypeDonnee> tousLesTypes = typeDonneeRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(fichier.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                return new ImportResultResponse(0, 0, 0, List.of("Fichier vide ou sans données"));
            }

            Row headerRow = sheet.getRow(0);
            java.util.Map<String, Integer> colIndex = new java.util.HashMap<>();
            if (headerRow != null) {
                for (int c = 0; c <= headerRow.getLastCellNum(); c++) {
                    Cell cell = headerRow.getCell(c);
                    if (cell != null) {
                        String header = getCellStringValue(cell);
                        if (header != null) colIndex.put(header.toLowerCase().trim(), c);
                    }
                }
            }

            boolean isNewFormat = colIndex.containsKey("nom") || colIndex.containsKey("prenom");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalLignes++;
                try {
                    if (isNewFormat) {
                        int iNom        = colIndex.getOrDefault("nom", 0);
                        int iPrenom     = colIndex.getOrDefault("prenom", 1);
                        int iEmail      = colIndex.getOrDefault("email", 2);
                        int iTel        = colIndex.getOrDefault("telephone", 3);
                        int iNaissance  = colIndex.getOrDefault("date_naissance", 4);
                        int iCnib       = colIndex.getOrDefault("numero_cnib", 5);
                        int iProfession = colIndex.getOrDefault("profession", 6);

                        String nom          = getCellStringValue(row.getCell(iNom));
                        String prenom       = getCellStringValue(row.getCell(iPrenom));
                        String email        = getCellStringValue(row.getCell(iEmail));
                        String telephone    = getCellStringValue(row.getCell(iTel));
                        String dateNaissance= getCellStringValue(row.getCell(iNaissance));
                        String cnib         = getCellStringValue(row.getCell(iCnib));
                        String profession   = getCellStringValue(row.getCell(iProfession));

                        if (nom == null || nom.isBlank()) throw new Exception("Colonne NOM vide");
                        if (prenom == null || prenom.isBlank()) throw new Exception("Colonne PRENOM vide");

                        Personne personne = trouverOuCreerPersonne(nom.trim(), prenom.trim(), email, telephone);

                        int count = 0;
                        if (telephone != null && !telephone.isBlank())
                            count += sauvegarderDonnee(personne, traitement, tousLesTypes, "Téléphone", telephone.trim(), erreurs, i);
                        if (email != null && !email.isBlank())
                            count += sauvegarderDonnee(personne, traitement, tousLesTypes, "Email", email.trim(), erreurs, i);
                        if (dateNaissance != null && !dateNaissance.isBlank())
                            count += sauvegarderDonnee(personne, traitement, tousLesTypes, "Date de naissance", dateNaissance.trim(), erreurs, i);
                        if (cnib != null && !cnib.isBlank())
                            count += sauvegarderDonnee(personne, traitement, tousLesTypes, "Numéro CNIB", cnib.trim(), erreurs, i);
                        if (profession != null && !profession.isBlank())
                            count += sauvegarderDonnee(personne, traitement, tousLesTypes, "Profession", profession.trim(), erreurs, i);

                        if (count == 0) throw new Exception("Aucune donnée valide sur cette ligne");
                        lignesImportees += count;

                    } else {
                        // Ancien format : valeur / date / usagerId / typeDonneeId
                        String valeur = getCellStringValue(row.getCell(0));
                        if (valeur == null || valeur.isBlank()) throw new Exception("La colonne 'valeur' est vide");

                        LocalDateTime dateCollecte = LocalDateTime.now();
                        Cell cellDate = row.getCell(1);
                        if (cellDate != null && cellDate.getCellType() != CellType.BLANK) {
                            try { dateCollecte = cellDate.getLocalDateTimeCellValue(); } catch (Exception ignored) {}
                        }

                        Long usagerId = getCellLongValue(row.getCell(2));
                        if (usagerId == null) throw new Exception("usagerId manquant ou invalide");

                        Long typeDonneeId = getCellLongValue(row.getCell(3));
                        if (typeDonneeId == null) throw new Exception("typeDonneeId manquant ou invalide");

                        Usager usager = usagerRepository.findById(usagerId)
                                .orElseThrow(() -> new RuntimeException("Usager introuvable id=" + usagerId));
                        TypeDonnee typeDonnee = typeDonneeRepository.findById(typeDonneeId)
                                .orElseThrow(() -> new RuntimeException("TypeDonnee introuvable id=" + typeDonneeId));

                        // Anti-doublon
                        Personne p = (usager.getPersonne() != null) ? usager.getPersonne() : null;
                        boolean doublon = estDoublon(p, typeDonnee, valeur, traitement.getIdTraitement());
                        if (!doublon) {
                            DonneePersonnelle donnee = new DonneePersonnelle();
                            donnee.setValeur(valeur);
                            donnee.setDateCollecte(dateCollecte);
                            donnee.setUsager(usager);
                            if (p != null) donnee.setPersonne(p);
                            donnee.setTypeDonnee(typeDonnee);
                            donnee.setTraitement(traitement);
                            donneePersonnelleRepository.save(donnee);
                            lignesImportees++;
                        } else {
                            erreurs.add("Ligne " + (i + 1) + " : doublon ignoré (" + typeDonnee.getNom() + " = " + valeur + ")");
                        }
                    }

                } catch (Exception e) {
                    erreurs.add("Ligne " + (i + 1) + " : " + e.getMessage());
                }
            }
        }

        if (lignesImportees > 0) {
            traitementService.incrementerNombreDonnee(traitementId, (long) lignesImportees);
        }

        return new ImportResultResponse(totalLignes, lignesImportees, totalLignes - lignesImportees, erreurs);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean estDoublon(Personne personne, TypeDonnee typeDonnee, String valeur, Long traitementId) {
        if (personne == null) return false;
        return donneePersonnelleRepository.findByTraitementId(traitementId)
                .stream()
                .anyMatch(d ->
                    d.getTypeDonnee() != null
                    && d.getTypeDonnee().getIdTypeDonnee().equals(typeDonnee.getIdTypeDonnee())
                    && d.getValeur() != null
                    && d.getValeur().equalsIgnoreCase(valeur)
                    && d.getPersonne() != null
                    && d.getPersonne().getId().equals(personne.getId())
                );
    }

    /** Sauvegarde une donnée dans le traitement + copie dans entrepôt (anti-doublon) */
    private int sauvegarderDonnee(Personne personne, Traitement traitement,
            List<TypeDonnee> tousLesTypes, String typeNom, String valeur,
            List<String> erreurs, int ligneNum) {

        TypeDonnee type = tousLesTypes.stream()
                .filter(t -> t.getNom().equalsIgnoreCase(typeNom))
                .findFirst().orElse(null);
        if (type == null) return 0;

        // Anti-doublon dans le traitement
        if (estDoublon(personne, type, valeur, traitement.getIdTraitement())) {
            return 0; // doublon silencieux dans l'import
        }

        // Sauvegarde dans le traitement
        DonneePersonnelle donnee = new DonneePersonnelle();
        donnee.setValeur(valeur);
        donnee.setDateCollecte(LocalDateTime.now());
        donnee.setPersonne(personne);
        donnee.setTypeDonnee(type);
        donnee.setTraitement(traitement);
        donneePersonnelleRepository.save(donnee);

        // Copie dans l'entrepôt si pas déjà présent
        boolean dejaEntrepot = donneePersonnelleRepository.findEntrepot()
                .stream()
                .anyMatch(d ->
                    d.getTypeDonnee() != null
                    && d.getTypeDonnee().getIdTypeDonnee().equals(type.getIdTypeDonnee())
                    && d.getValeur() != null
                    && d.getValeur().equalsIgnoreCase(valeur)
                    && d.getPersonne() != null
                    && d.getPersonne().getId().equals(personne.getId())
                );
        if (!dejaEntrepot) {
            DonneePersonnelle copie = new DonneePersonnelle();
            copie.setValeur(valeur);
            copie.setDateCollecte(LocalDateTime.now());
            copie.setPersonne(personne);
            copie.setTypeDonnee(type);
            copie.setTraitement(null); // entrepôt
            donneePersonnelleRepository.save(copie);
        }

        return 1;
    }

    private Personne trouverOuCreerPersonne(String nom, String prenom, String email, String telephone) {
        if (email != null && !email.isBlank()) {
            java.util.Optional<Personne> existant = personneRepository.findByEmail(email.trim());
            if (existant.isPresent()) return existant.get();
        }
        if (telephone != null && !telephone.isBlank()) {
            java.util.Optional<Personne> existant = personneRepository.findByTelephone(telephone.trim());
            if (existant.isPresent()) return existant.get();
        }
        Personne p = new Personne();
        p.setNom(nom); p.setPrenom(prenom);
        p.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        p.setTelephone(telephone != null && !telephone.isBlank() ? telephone.trim() : null);
        p.setDateCreation(LocalDateTime.now());
        p.setDateModification(LocalDateTime.now());
        return personneRepository.save(p);
    }

    public List<DonneePersonnelleResponse> listerDonnees() {
        return donneePersonnelleRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DonneePersonnelleResponse> listerParUsager(Long usagerId) {
        return donneePersonnelleRepository.findByUsagerId(usagerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DonneePersonnelleResponse> listerParPersonne(Long personneId) {
        return donneePersonnelleRepository.findByPersonneId(personneId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DonneePersonnelleResponse> listerParTraitement(Long traitementId) {
        return donneePersonnelleRepository.findByTraitementId(traitementId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> null;
        };
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING  -> Long.parseLong(cell.getStringCellValue().trim());
                default      -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private DonneePersonnelleResponse toResponse(DonneePersonnelle d) {
        String nomUsager = d.getUsager() != null
                ? d.getUsager().getPrenom() + " " + d.getUsager().getNom()
                : null;
        String nomPersonne = d.getPersonne() != null
                ? d.getPersonne().getPrenom() + " " + d.getPersonne().getNom()
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