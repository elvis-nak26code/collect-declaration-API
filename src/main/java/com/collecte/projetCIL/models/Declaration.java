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

    // ── Nouveaux champs responsable (infos entreprise) ────────────────────
    @Column(name = "nom_raison_sociale")
    private String nomRaisonSociale;

    private String rccm;

    @Column(name = "secteur_activite")
    private String secteurActivite;

    private String adresse;

    @Column(name = "boite_postale")
    private String boitePostale;

    private String ville;

    // telephone et adresse_email déjà portés par Utilisateur, on les duplique ici
    // pour la déclaration (données du responsable du traitement, pas du DPO)
    @Column(name = "telephone_responsable")
    private String telephoneResponsable;

    @Column(name = "adresse_email_responsable")
    private String adresseEmailResponsable;

    @Column(name = "activite_principale")
    private String activitePrincipale;

    @Enumerated(EnumType.STRING)
    private StatutDeclaration statut;

    /**
     * AUTOMATIQUE : déclaration pré-créée en BROUILLON en même temps que le
     * traitement (par l'Utilisateur Métier) — ne doit jamais apparaître dans
     * la liste "Mes déclarations" du DPO.
     * MANUELLE : déclaration créée explicitement par le DPO via "Déclarer".
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "origine_declaration")
    private com.collecte.projetCIL.enums.OrigineDeclaration origineDeclaration;

    // ── Relations ─────────────────────────────────────────────────────────

    // Traitement source qui a déclenché la création de cette déclaration
    @ManyToOne
    @JoinColumn(name = "traitement_id")
    private Traitement traitement;

    @ManyToOne
    @JoinColumn(name = "dpo_id")
    private DPO dpo;

    @ManyToOne
    @JoinColumn(name = "cil_id")
    private CIL cil;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
    private List<HistoriqueDeclaration> historiques;
}