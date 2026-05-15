package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "declaration_normale")
@PrimaryKeyJoinColumn(name = "declaration_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationNormale extends Declaration {

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

    @Column(name = "type_traitement")
    private String typeTraitement;

    @Column(name = "description_procedure_manuelle")
    private Boolean descriptionProcedureManuelle;

    @Column(name = "caracteristiques_techniques")
    private String caracteristiquesTechniques;

    @Column(name = "caracteristiques_systeme")
    private String caracteristiquesSysteme;

    @Column(name = "politique_acces_systemes")
    private Boolean politiqueAccesSystemes;

    @Column(name = "modalites_diffusion_resultats")
    private Boolean modalitesDiffusionResultats;

    @Column(name = "protocole_recherche")
    private Boolean protocoleRecherche;

    @Column(name = "description_connexion_fichiers")
    private Boolean descriptionConnexionFichiers;

    @Column(name = "motifs_interconnexion")
    private String motifsInterconnexion;

    @Column(name = "identite_fichiers_interconnexion")
    private String identiteFichiersInterconnexion;
}
