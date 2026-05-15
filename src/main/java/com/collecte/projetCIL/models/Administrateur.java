package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "administrateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    private String fonction;

    // 1 Administrateur -> 0..* DemandeAcces (traite)
    @OneToMany(mappedBy = "administrateur", cascade = CascadeType.ALL)
    private List<DemandeAcces> demandesAcces;
}
