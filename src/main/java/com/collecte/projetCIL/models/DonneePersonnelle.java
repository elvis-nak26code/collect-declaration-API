package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "donnee_personnelle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonneePersonnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donnee")
    private Long idDonnee;

    private String valeur;

    @Column(name = "date_collecte")
    private LocalDateTime dateCollecte;

    // 1..* DonneePersonnelle -> 1 Personne (la vraie personne concernée)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personne_id")
    private Personne personne;

    // 1..* DonneePersonnelle -> 1 Usager (compte ayant saisi — conservé pour compatibilité)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usager_id")
    private Usager usager;

    // 1..* DonneePersonnelle -> 1 TypeDonnee
    @ManyToOne
    @JoinColumn(name = "type_donnee_id")
    private TypeDonnee typeDonnee;

    // Traitement auquel cette donnée est rattachée (peut être null pour données orphelines)
    @ManyToOne
    @JoinColumn(name = "traitement_id", nullable = true)
    private Traitement traitement;
}
