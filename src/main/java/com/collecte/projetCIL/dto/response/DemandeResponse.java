package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutDemande;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeResponse {

    private Long idDemande;
    private LocalDate dateDemande;
    private String typeDemande;
    private String descriptionDemande;
    private String nouvelleValeur;
    private StatutDemande statutDemande;
    private String reponse;
    private String motifRejet;
    private LocalDate dateTraitement;

    // Usager
    private Long usagerId;
    private String usagerNomComplet;

    // UtilisateurMetier
    private Long utilisateurMetierId;
    private String utilisateurMetierNomComplet;

    // Donnée concernée
    private Long donneeId;
    private String donneeValeur;
}
