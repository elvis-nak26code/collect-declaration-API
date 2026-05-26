package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class InscriptionRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String typeUtilisateur; // USAGER, CIL, DPO, DG, UTILISATEUR_METIER
    // Champs spécifiques selon le type
    private String telephone;
    private String adresse;
    private String matricule;
    private String organisme;
    private String adresseProfessionnelle;
    private String service;
    private String fonction;
    private String niveauResponsabilite;
    private String idDg;
}
