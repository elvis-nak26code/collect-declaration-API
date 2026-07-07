package com.collecte.projetCIL.dto.request;

import com.collecte.projetCIL.enums.NatureDemande;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DeclarationNormaleRequest {

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

    // ── Champs spécifiques à DeclarationNormale ──────────────────────────
    private String denominationTraitement;
    private String finaliteTraitement;
    private String texteJuridique;
    private String categoriesPersonnesConcernees;
    private Integer nombrePersonnesConcernees;
    private String typeTraitement;
    private Boolean descriptionProcedureManuelle;
    private String caracteristiquesTechniques;
    private String caracteristiquesSysteme;
    private Boolean politiqueAccesSystemes;
    private Boolean modalitesDiffusionResultats;
    private Boolean protocoleRecherche;
    private Boolean descriptionConnexionFichiers;
    private String motifsInterconnexion;
    private String identiteFichiersInterconnexion;
    private String intituleTraitement;
    private String supportTraitement;
    private String categoriesDonneesCollectees;
    private Boolean donneesSensibles;
    private String natureDonneesSensibles;

    // ── Champs communs de signature / transfert (Declaration) ────────────
    private String serviceResponsable;
    private LocalDate dateSignature;
    private String lieuSignature;
    private String paysDestination;
    private String garantiesProtectionEtranger;
}