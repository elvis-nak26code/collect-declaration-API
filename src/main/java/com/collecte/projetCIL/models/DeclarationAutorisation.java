package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.NatureDemande;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "declaration_autorisation")
@PrimaryKeyJoinColumn(name = "declaration_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationAutorisation extends Declaration {

    @Column(name = "denomination_traitement")
    private String denominationTraitement;

    @Column(name = "finalite_traitement")
    private String finaliteTraitement;

    @Column(name = "texte_juridique")
    private String texteJuridique;

    @Column(name = "categories_personnes_concernees")
    private String categoriesPersonnesConcernees;

    @Column(name = "nombre_personnes_concernees")
    private Integer nombrePersonnesConcernees;
    
// ce attribut est deja presnent dans la class mere 
    // @Enumerated(EnumType.STRING)
    // @Column(name = "nature_demande")
    // private NatureDemande natureDemande;

    @Column(name = "type_traitement")
    private String typeTraitement;

    @Column(name = "caracteristiques_techniques")
    private String caracteristiquesTechniques;

    @Column(name = "fonctionnalites_systeme")
    private String fonctionnalitesSysteme;

    @Column(name = "certification_securite")
    private String certificationSecurite;

    @Column(name = "politique_acces_systemes")
    private Boolean politiqueAccesSystemes;

    @Column(name = "description_fichier")
    private String descriptionFichier;

    @Column(name = "mode_transfert")
    private String modeTransfert;

    @Column(name = "traitement_donnees_sante")
    private Boolean traitementDonneesSante;

    @Column(name = "professional_sante")
    private Boolean professionalSante;

    @Column(name = "destinataire_adresse")
    private String destinataireAdresse;

    @Column(name = "texte_juridique_communication")
    private String texteJuridiqueCommunication;

    @Column(name = "modalites_diffusion_resultats")
    private String modalitesDiffusionResultats;

    @Column(name = "destinataire_cie")
    private String destinataireCie;

    @Column(name = "connexion_fichiers")
    private Boolean connexionFichiers;

    @Column(name = "categories_donnees_interconnexion")
    private String categoriesDonneesInterconnexion;

    @Column(name = "duree_interconnexion")
    private String dureeInterconnexion;

    @Column(name = "identite_fichiers_interconnexion")
    private String identiteFichiersInterconnexion;

    @Column(name = "transfert_pays_etranger")
    private Boolean transfertPaysEtranger;

    @Column(name = "recours_sous_traitant")
    private Boolean recoursSousTraitant;

    @Column(name = "roles_sous_traitants")
    private String rolesSousTraitants;

    @Column(name = "categories_personnes_acces")
    private String categoriesPersonnesAcces;

    @Column(name = "politique_acces_batiments")
    private Boolean politiqueAccesBatiments;

    @Column(name = "mesures_securite")
    private String mesuresSecurite;

    @Column(name = "description_sensibilisation")
    private String descriptionSensibilisation;

    @Column(name = "pays_destination_protection_donnees")
    private Boolean paysDestinationProtectionDonnees;

    @Column(name = "description_fichier_transfert")
    private String descriptionFichierTransfert;

    @Column(name = "nombre_personnes_transfert")
    private Integer nombrePersonnesTransfert;

    @Column(name = "categories_donnees_transfert")
    private String categoriesDonneesTransfert;

    @Column(name = "fondement_juridique")
    private String fondementJuridique;

    @Column(name = "consentement_personnes_concernees")
    private Boolean consentementPersonnesConcernees;

    @Column(name = "methode_recueil_consentement")
    private String methodeRecueilConsentement;

    @Column(name = "mesures_securite_transfert")
    private String mesuresSecuriteTransfert;

    @Column(name = "lieu_stockage")
    private String lieuStockage;

    @Column(name = "communication_autres_organismes")
    private Boolean communicationAutresOrganismes;

    @Column(name = "destinataire_nom_prenom")
    private String destinataireNomPrenom;

    @Column(name = "duree_conservation_sante")
    private String dureeConservationSante;

    @Column(name = "origine_donnees")
    private String origineDonnees;

    @Column(name = "finalite_sante")
    private String finaliteSante;

    @Column(name = "pays_destination_transfert")
    private String paysDestinationTransfert;
}