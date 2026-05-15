package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "declaration_systeme_video_surveillance")
@PrimaryKeyJoinColumn(name = "declaration_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationSystemeVideoSurveillance extends Declaration {

    private String finalites;

    @Column(name = "adresse_installation")
    private String adresseInstallation;

    @Column(name = "nature_environnement")
    private String natureEnvironnement;

    @Column(name = "emplacement_cameras")
    private String emplacementCameras;

    @Column(name = "nombre_total_cameras")
    private Integer nombreTotalCameras;

    @Column(name = "modele_dispositif")
    private String modeleDispositif;

    @Column(name = "visualisation_temps_reel")
    private Boolean visualisationTempsReel;

    @Column(name = "mode_transfert")
    private String modeTransfert;

    @Column(name = "son_de_son")
    private Boolean sonDeSon;

    @Column(name = "type_enregistrement")
    private String typeEnregistrement;

    @Column(name = "nature_enregistrement")
    private String natureEnregistrement;

    @Column(name = "liaison_reseau")
    private String liaisonReseau;

    @Column(name = "utilisation_systemes_experts")
    private Boolean utilisationSystemesExperts;

    @Column(name = "description_systemes_experts")
    private String descriptionSystemesExperts;

    @Column(name = "fonctionnalites_traitement")
    private String fonctionnalitesTraitement;

    @Column(name = "acces_images_distance")
    private Boolean accesImagesDistance;

    @Column(name = "acces_physique")
    private String accesPhysique;

    @Column(name = "acces_logique")
    private String accesLogique;

    @Column(name = "mesures_suppression")
    private Boolean mesuresSuppression;

    private String attribute;

    @Column(name = "localisation_pictogrammes")
    private String localisationPictogrammes;
}
