package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "usager")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Usager extends Utilisateur {

    private String telephone;
    private String adresse;
    private String matricule;

    // 1 Usager -> 1..* DonneePersonnelle
    @OneToMany(mappedBy = "usager", cascade = CascadeType.ALL)
    private List<DonneePersonnelle> donneesPersonnelles;

    // 1 Usager -> 0..* Demande
    @OneToMany(mappedBy = "usager", cascade = CascadeType.ALL)
    private List<Demande> demandes;

    // 1 Usager -> 1..* Plainte
    @OneToMany(mappedBy = "usager", cascade = CascadeType.ALL)
    private List<Plainte> plaintes;
}
