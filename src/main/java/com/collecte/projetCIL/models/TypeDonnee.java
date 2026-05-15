package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "type_donnee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeDonnee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_donnee")
    private Long idTypeDonnee;

    private String nom;
    private Boolean sensible;

    @OneToMany(mappedBy = "typeDonnee", cascade = CascadeType.ALL)
    private List<DonneePersonnelle> donneesPersonnelles;
}
