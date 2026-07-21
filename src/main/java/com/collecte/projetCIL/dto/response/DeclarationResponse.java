package com.collecte.projetCIL.dto.response;

import java.time.LocalDate;

import com.collecte.projetCIL.enums.NatureDemande;
import com.collecte.projetCIL.enums.StatutDeclaration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeclarationResponse {

    private Long idDeclaration;
    private String motifRejetCil;
    private String typeDeclaration;
    private LocalDate dateSoumission;
    private StatutDeclaration statut;
    private String origineDeclaration;

    private Long traitementId;
    private String traitementDescription;
    private String traitementNom;
    private Long dpoId;
    private String dpoNomPrenom;
    private Long cilId;
    private String cilNomPrenom;

    private String secteur;
    private NatureDemande natureDemande;
    private String responsableDeclaration;
    private String contactConfidentialite;
    private LocalDate dateMiseEnOeuvre;
    private LocalDate dateReceptiose;
    private String numeroRD;
    private LocalDate dateRecepisse;
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
    private String paysDestination;
    private String garantiesProtectionEtranger;
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
    private String serviceResponsable;
    private LocalDate dateSignature;
    private String lieuSignature;
    private String nomRaisonSociale;
    private String rccm;
    private String secteurActivite;
    private String adresse;
    private String boitePostale;
    private String ville;
    private String telephoneResponsable;
    private String adresseEmailResponsable;
    private String activitePrincipale;

    private String denominationTraitement;
    private String finaliteTraitement;
    private String texteJuridique;
    private String categoriesPersonnesConcernees;
    private Integer nombrePersonnesConcernees;
    private String typeTraitement;
    private String caracteristiquesTechniques;
    private String modeTransfert;
    private String certificationSecurite;

    private Boolean descriptionProcedureManuelle;
    private String caracteristiquesSysteme;
    private Boolean politiqueAccesSystemes;
    private Boolean modalitesDiffusionResultatsBool;
    private Boolean protocoleRecherche;
    private Boolean descriptionConnexionFichiers;
    private String motifsInterconnexion;
    private String identiteFichiersInterconnexion;
    private String intituleTraitement;
    private String supportTraitement;
    private String categoriesDonneesCollectees;
    private Boolean donneesSensibles;
    private String natureDonneesSensibles;

    private String caracteristiquesMainStructure;
    private Boolean donneesConnexion;
    private String descriptionDonneesConnexion;
    private Boolean cookies;
    private String descriptionCookies;
    private String dureeConservationCookies;
    private String telechargementTraitement;
    private String urlSite;
    private String typeCookies;
    private Boolean consentementCookies;
    private Boolean formulairesEnLigne;
    private String donneesFormulaires;

    private String finalites;
    private String adresseInstallation;
    private String natureEnvironnement;
    private String emplacementCameras;
    private Integer nombreTotalCameras;
    private String modeleDispositif;
    private Boolean visualisationTempsReel;
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

    private String fonctionnalitesSysteme;
    private String descriptionFichier;
    private Boolean traitementDonneesSante;
    private Boolean professionalSante;
    private String modalitesDiffusionResultats;
    private String destinataireCie;
    private Boolean connexionFichiers;
    private String categoriesDonneesInterconnexion;
    private String dureeInterconnexion;
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
    private String descriptionSensibilisation;
    private String finaliteSante;
    private String paysDestinationTransfert;
}