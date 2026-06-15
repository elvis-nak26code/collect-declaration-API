package com.collecte.projetCIL.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.collecte.projetCIL.enums.NatureDemande;

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

    /**
     * Optionnel : si null, le traitement est créé sans session de collecte.
     * Il pourra être lié à une session plus tard via PATCH /api/traitements/{id}/session.
     */
    private Long sessionCollecteId;

    // ── Champs communs pré-remplissage Declaration (base) ────────────────
    private String secteur;
    private String lieuStockage;
    private String dureeConservationDeclaration;
    private LocalDate dateMiseEnOeuvre;
    private Boolean transfertEtranger;
    private Boolean sousTraitance;
    private Boolean communicationTiers;

    // ── Étape 3 : Identification & Responsable ────────────────────────────
    private String nomPrenomResponsable;
    private String fonctionResponsable;
    private String contactConfidentialite;
    private NatureDemande natureDemande;

    // ── Étape 4 : Données traitées ────────────────────────────────────────
    private String categoriesDonnees;
    private String origineDonnees;

    // ── Étape 4 : Communication & destinataires ───────────────────────────
    private Boolean destinataireConformeCil;

    // ── Étape 4 : Mesures de sécurité ────────────────────────────────────
    private String mesuresSecurite;
    private Boolean mesuresSensibilisation;
    private Boolean politiqueAccesBatiments;
    private String categoriesPersonnesAcces;

    // ── Étape 2 : Identification du traitement (spécifique DeclarationNormale) ──
    private String denominationTraitement;
    private String finaliteTraitement;
    private String categoriesPersonnesConcernees;
    private Integer nombrePersonnesConcernees;
    private String typeTraitement;

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