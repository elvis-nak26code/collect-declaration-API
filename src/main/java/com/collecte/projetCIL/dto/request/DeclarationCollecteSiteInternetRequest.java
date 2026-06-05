package com.collecte.projetCIL.dto.request;

import com.collecte.projetCIL.enums.NatureDemande;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DeclarationCollecteSiteInternetRequest {

    // ── Champs hérités de Declaration ────────────────────────────────────
    private LocalDate dateSoumission;
    private String secteur;
    private NatureDemande natureDemande;
    private String responsableDeclaration;
    private String contactConfidentialite;
    private LocalDate dateMiseEnOeuvre;
    private String categoriesDonnees;
    private String origineDonnees;
    private String dureeConservation;
    private String lieuStockage;
    private Boolean communicationAutresOrganismes;
    private String destinataireNom;
    private String destinataireAdresse;
    private String texteJuridiqueCommunication;
    private String finaliteCommunication;
    private Boolean destinataireConformeCil;
    private Boolean transfertPaysEtranger;
    private Boolean recoursSousTraitant;
    private Boolean contratConfidentialiteSousTraitant;
    private String rolesSousTraitants;
    private String categoriesPersonnesAcces;
    private Boolean politiqueAccesBatiments;
    private String mesuresSecurite;
    private Boolean mesuresSensibilisation;
    private String moyensInformationDroits;
    private String moyensExerciceDroits;
    private String coordonneesExerciceDroits;
    private String delaiCommunicationDroits;
    private String nomPrenomResponsable;
    private String fonctionResponsable;

    // ── Identifiant du traitement associé ────────────────────────────────
    private Long traitementId;

    // ── Champs spécifiques à DeclarationCollecteSiteInternet ─────────────
    private String denominationTraitement;
    private String finaliteTraitement;
    private String texteJuridique;
    private String categoriesPersonnesConcernees;
    private String caracteristiquesMainStructure;
    private String caracteristiquesTechniques;
    private String typeTraitement;
    private Boolean donneesConnexion;
    private String descriptionDonneesConnexion;
    private Boolean cookies;
    private String descriptionCookies;
    private String dureeConservationCookies;
    private String telechargementTraitement;
}
