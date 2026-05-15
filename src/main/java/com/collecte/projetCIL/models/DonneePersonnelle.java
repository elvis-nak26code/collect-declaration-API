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

    // 1..* DonneePersonnelle -> 1 Usager
    @ManyToOne
    @JoinColumn(name = "usager_id")
    private Usager usager;

    // 1..* DonneePersonnelle -> 1 TypeDonnee
    @ManyToOne
    @JoinColumn(name = "type_donnee_id")
    private TypeDonnee typeDonnee;
}
