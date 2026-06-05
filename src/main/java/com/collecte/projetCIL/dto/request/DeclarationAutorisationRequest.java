package com.collecte.projetCIL.dto.request;

import java.time.LocalDate;

import com.collecte.projetCIL.enums.NatureDemande;

import lombok.Data;

@Data
public class DeclarationAutorisationRequest {

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
    private String descriptionSensibilisation;
    private String moyensInformationDroits;
    private String moyensExerciceDroits;
    private String coordonneesExerciceDroits;
    private String delaiCommunicationDroits;
    private String nomPrenomResponsable;
    private String fonctionResponsable;

    // ── Identifiant du traitement associé ────────────────────────────────
    private Long traitementId;

    // ── Champs spécifiques à DeclarationAutorisation ─────────────────────
    private String denominationTraitement;
    private String finaliteTraitement;
    private String texteJuridique;
    private String categoriesPersonnesConcernees;
    private Integer nombrePersonnesConcernees;
    private String typeTraitement;
    private String caracteristiquesTechniques;
    private String fonctionnalitesSysteme;
    private String certificationSecurite;
    private Boolean politiqueAccesSystemes;
    private String descriptionFichier;
    private String modeTransfert;
    private Boolean traitementDonneesSante;
    private Boolean professionalSante;
    private String texteJuridiqueCommunication2;
    private String modalitesDiffusionResultats;
    private String destinataireCie;
    private Boolean connexionFichiers;
    private String categoriesDonneesInterconnexion;
    private String dureeInterconnexion;
    private String identiteFichiersInterconnexion;
    private Boolean paysDestinationProtectionDonnees;
    private String descriptionFichierTransfert;
    private Integer nombrePersonnesTransfert;
    private String categoriesDonneesTransfert;
    private String fondementJuridique;
    private Boolean consentementPersonnesConcernees;
    private String methodeRecueilConsentement;
    private String mesuresSecuriteTransfert;
    private String destinataireNomPrenom;
    private String dureeConservationSante;
}