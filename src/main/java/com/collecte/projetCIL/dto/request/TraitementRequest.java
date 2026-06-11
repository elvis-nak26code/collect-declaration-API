package com.collecte.projetCIL.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TraitementRequest {

    // ── Champs Traitement ─────────────────────────────────────────────────
    private String nom;
    private String department;
    private String description;
    private String texte;
    private String certificationSecurite;
    private Integer dureeConservation;
    private LocalDateTime dateFin;
    private Long utilisateurMetierId;
    private Long sessionCollecteId;

    // ── Champs communs pré-remplissage Declaration (base) ────────────────
    private String secteur;
    private String lieuStockage;
    private String dureeConservationDeclaration;
    private LocalDate dateMiseEnOeuvre;
    private Boolean transfertEtranger;
    private Boolean sousTraitance;
    private Boolean communicationTiers;

    // ── Informations responsable → Declaration ────────────────────────────
    private String nomRaisonSociale;
    private String rccm;
    private String secteurActivite;
    private String adresse;
    private String boitePostale;
    private String ville;
    private String telephone;
    private String adresseEmail;
    private String activitePrincipale;
}