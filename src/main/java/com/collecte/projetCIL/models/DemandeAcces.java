package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "demande_acces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAcces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande_acces")
    private StatutDemandeAcces statutDemandeAcces;
    
    @Column(name = "motif_demande")
    private String motifDemande;
    
    @Column(name = "motif_rejet")
    private String motifRejet;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    // 1 Utilisateur -> 0..1 DemandeAcces
    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // ManyToOne Administrateur (traite)
    @ManyToOne
    @JoinColumn(name = "administrateur_id")
    private Administrateur administrateur;
}
