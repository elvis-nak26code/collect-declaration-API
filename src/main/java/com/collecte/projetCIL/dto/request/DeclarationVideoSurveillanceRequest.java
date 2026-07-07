package com.collecte.projetCIL.dto.request;

import com.collecte.projetCIL.enums.NatureDemande;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DeclarationVideoSurveillanceRequest {

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

    // ── Champs spécifiques à DeclarationSystemeVideoSurveillance ─────────
    private String finalites;
    private String adresseInstallation;
    private String natureEnvironnement;
    private String emplacementCameras;
    private Integer nombreTotalCameras;
    private String modeleDispositif;
    private Boolean visualisationTempsReel;
    private String modeTransfert;
    private Boolean sonDeSon;
    private String typeEnregistrement;
    private String natureEnregistrement;
    private String liaisonReseau;
    private Boolean utilisationSystemesExperts;
    private String descriptionSystemesExperts;
    private String fonctionnalitesTraitement;
    private Boolean accesImagesDistance;
    private String accesPhysique;
    private String accesLogique;
    private Boolean mesuresSuppression;
    private String attribute;
    private String localisationPictogrammes;
    private String dureeConservationVideo;
    private String modalitesAccesDistance;
    private String personnesHabilitees;

    // ── Champs communs de signature / transfert (Declaration) ────────────
    private String serviceResponsable;
    private LocalDate dateSignature;
    private String lieuSignature;
    private String paysDestination;
    private String garantiesProtectionEtranger;
}