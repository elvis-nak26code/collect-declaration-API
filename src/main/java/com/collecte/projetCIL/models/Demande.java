package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "demande")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;

    @Column(name = "date_demande")
    private LocalDate dateDemande;

    @Column(name = "type_demande")
    private String typeDemande;  // "MODIFICATION" ou "SUPPRESSION"

    @Column(name = "description_demande")
    private String descriptionDemande;

    /** Nouvelle valeur souhaitée (uniquement pour MODIFICATION). */
    @Column(name = "nouvelle_valeur")
    private String nouvelleValeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande")
    private StatutDemande statutDemande;

    private String reponse;

    @Column(name = "date_traitement")
    private LocalDate dateTraitement;

    @Column(name = "motif_rejet")
    private String motifRejet;

    // Personne concernée par la demande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personne_id")
    private Personne personne;

    // Usager qui fait la demande (conservé pour compatibilité)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usager_id")
    private Usager usager;

    // UtilisateurMetier qui traite la demande
    @ManyToOne
    @JoinColumn(name = "utilisateur_metier_id")
    private UtilisateurMetier utilisateurMetier;

    // Donnée personnelle concernée par la demande
    @ManyToOne
    @JoinColumn(name = "donnee_id", nullable = true)
    private DonneePersonnelle donneePersonnelle;
}
