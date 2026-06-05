package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.NatureDemande;
import com.collecte.projetCIL.enums.StatutDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Réponse commune renvoyée après la création ou la consultation
 * de n'importe quel type de déclaration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationResponse {

    private Long idDeclaration;
    private String typeDeclaration;   // "NORMALE" | "COLLECTE_SITE" | "VIDEO_SURVEILLANCE" | "AUTORISATION"
    private LocalDate dateSoumission;
    private String secteur;
    private NatureDemande natureDemande;
    private StatutDeclaration statut;
    private String responsableDeclaration;
    private String contactConfidentialite;
    private LocalDate dateMiseEnOeuvre;

    // Qui a soumis
    private Long dpoId;
    private String dpoNomPrenom;

    // Traitement associé
    private Long traitementId;
    private String traitementDescription;
}
