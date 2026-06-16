package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutPlainte;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "plainte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plainte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plainte")
    private Long idPlainte;

    @Column(name = "date_plainte")
    private LocalDate datePlainte;

    private String lieu;

    @Column(name = "objet_plainte")
    private String objetPlainte;

    @Column(name = "description_plainte")
    private String descriptionPlainte;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_plainte")
    private StatutPlainte statutPlainte;

    @Column(name = "date_traitement")
    private LocalDate dateTraitement;

    @Column(name = "decision_cil")
    private String decisionCil;

    @Column(name = "date_decision")
    private LocalDate dateDecision;

    @Column(name = "demande_prealable_effectuee")
    private Boolean demandePrealableEffectuee;

    // Usager qui a déposé la plainte (optionnel — peut être null si CIL → DPO)
    @ManyToOne
    @JoinColumn(name = "usager_id", nullable = true)
    private Usager usager;

    // CIL émetteur de la plainte (pour plaintes CIL → DPO)
    @ManyToOne
    @JoinColumn(name = "cil_id", nullable = true)
    private CIL cil;

    // DPO destinataire de la plainte émise par la CIL
    @ManyToOne
    @JoinColumn(name = "dpo_id", nullable = true)
    private DPO dpo;
}
