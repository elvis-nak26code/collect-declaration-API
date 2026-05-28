package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandeAccesResponse {

    // --- Infos de la demande ---
    private Long idDemande;
    private StatutDemandeAcces statutDemandeAcces;
    private LocalDateTime dateDemande;
    private LocalDateTime dateValidation;
    private String motif;

    // --- Infos de l'utilisateur ---
    private Long utilisateurId;
    private String nom;
    private String prenom;
    private String email;
    private String typeUtilisateur;
    private StatutUtilisateur statutUtilisateur;
    private LocalDateTime dateCreationCompte;
    private String ville;
    private String telephone;
    private String organisme;
    private String fonction;

    // --- Infos sur le traitement ---
    private String adminTraitantNom;
}