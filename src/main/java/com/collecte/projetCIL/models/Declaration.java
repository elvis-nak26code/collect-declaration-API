package com.collecte.projetCIL.models;

import java.time.LocalDate;
import java.util.List;

import com.collecte.projetCIL.enums.NatureDemande;
import com.collecte.projetCIL.enums.StatutDeclaration;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "declaration")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_declaration")
    private Long idDeclaration;

    @Column(name = "date_soumission")
    private LocalDate dateSoumission;

    private String secteur;

    @Enumerated(EnumType.STRING)
@Column(name = "nature_demande")
private NatureDemande natureDemande;

    @Column(name = "responsable_declaration")
    private String responsableDeclaration;

    @Column(name = "contact_confidentialite")
    private String contactConfidentialite;

    @Column(name = "date_mise_en_oeuvre")
    private LocalDate dateMiseEnOeuvre;

    @Column(name = "date_receptiose")
    private LocalDate dateReceptiose;

    // jai ajouter des atributs cici je doit verifier plus tard.
    @Column(name = "numero_rd")
private String numeroRD;

@Column(name = "date_recepisse")
private LocalDate dateRecepisse;

@Column(name = "categories_donnees")
private String categoriesDonnees;

@Column(name = "origine_donnees")
private String origineDonnees;

@Column(name = "duree_conservation")
private String dureeConservation;

@Column(name = "lieu_stockage")
private String lieuStockage;

@Column(name = "communication_autres_organismes")
private Boolean communicationAutresOrganismes;

@Column(name = "destinataire_nom")
private String destinataireNom;

@Column(name = "destinataire_adresse")
private String destinataireAdresse;

@Column(name = "texte_juridique_communication")
private String texteJuridiqueCommunication;

@Column(name = "finalite_communication")
private String finaliteCommunication;

@Column(name = "destinataire_conforme_cil")
private Boolean destinataireConformeCil;

@Column(name = "transfert_pays_etranger")
private Boolean transfertPaysEtranger;

@Column(name = "recours_sous_traitant")
private Boolean recoursSousTraitant;

@Column(name = "contrat_confidentialite_sous_traitant")
private Boolean contratConfidentialiteSousTraitant;

@Column(name = "roles_sous_traitants")
private String rolesSousTraitants;

@Column(name = "categories_personnes_acces")
private String categoriesPersonnesAcces;

@Column(name = "politique_acces_batiments")
private Boolean politiqueAccesBatiments;

@Column(name = "mesures_securite")
private String mesuresSecurite;

@Column(name = "mesures_sensibilisation")
private Boolean mesuresSensibilisation;

@Column(name = "moyens_information_droits")
private String moyensInformationDroits;

@Column(name = "moyens_exercice_droits")
private String moyensExerciceDroits;

@Column(name = "coordonnees_exercice_droits")
private String coordonneesExerciceDroits;

@Column(name = "delai_communication_droits")
private String delaiCommunicationDroits;

@Column(name = "nom_prenom_responsable")
private String nomPrenomResponsable;

@Column(name = "fonction_responsable")
private String fonctionResponsable;

    @Enumerated(EnumType.STRING)
    private StatutDeclaration statut;

    // ManyToOne DPO (soumet)
    @ManyToOne
    @JoinColumn(name = "dpo_id")
    private DPO dpo;

    // ManyToOne CIL (verifie)
    @ManyToOne
    @JoinColumn(name = "cil_id")
    private CIL cil;

    // 1 Declaration -> 0..* HistoriqueDeclaration
    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
    private List<HistoriqueDeclaration> historiques;
}
