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
    private String typeDemande;

    @Column(name = "description_demande")
    private String descriptionDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande")
    private StatutDemande statutDemande;

    private String reponse;

    @Column(name = "date_traitement")
    private LocalDate dateTraitement;

    @Column(name = "motif_rejet")
    private String motifRejet;

    // ManyToOne Usager (fait)
    @ManyToOne
    @JoinColumn(name = "usager_id")
    private Usager usager;

    // ManyToOne UtilisateurMetier (traite)
    @ManyToOne
    @JoinColumn(name = "utilisateur_metier_id")
    private UtilisateurMetier utilisateurMetier;
}
