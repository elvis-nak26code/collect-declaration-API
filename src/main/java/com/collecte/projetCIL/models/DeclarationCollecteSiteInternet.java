package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "declaration_collecte_site_internet")
@PrimaryKeyJoinColumn(name = "declaration_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationCollecteSiteInternet extends Declaration {

    @Column(name = "denomination_traitement")
    private String denominationTraitement;

    @Column(name = "finalite_traitement")
    private String finaliteTraitement;

    @Column(name = "texte_juridique")
    private String texteJuridique;

    @Column(name = "categories_personnes_concernees")
    private String categoriesPersonnesConcernees;

    @Column(name = "caracteristiques_principales_structure")
    private String caracteristiquesMainStructure;

    @Column(name = "caracteristiques_techniques")
    private String caracteristiquesTechniques;

    @Column(name = "type_traitement")
    private String typeTraitement;

    @Column(name = "donnees_connexion")
    private Boolean donneesConnexion;

    @Column(name = "description_donnees_connexion")
    private String descriptionDonneesConnexion;

    private Boolean cookies;

    @Column(name = "description_cookies")
    private String descriptionCookies;

    @Column(name = "duree_conservation_cookies")
    private String dureeConservationCookies;

    @Column(name = "telechargement_traitement")
    private String telechargementTraitement;
}
