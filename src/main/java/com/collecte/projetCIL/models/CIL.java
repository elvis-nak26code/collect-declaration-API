package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cil")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CIL extends Utilisateur {

    private String service;
    private String fonction;

    @Column(name = "niveau_responsabilite")
    private String niveauResponsabilite;

    // 1 CIL -> 0..* Plainte (traite)
    @OneToMany(mappedBy = "cil", cascade = CascadeType.ALL)
    private List<Plainte> plaintes;

    // 1 CIL -> 0..* Declaration (verifie)
    @OneToMany(mappedBy = "cil", cascade = CascadeType.ALL)
    private List<Declaration> declarations;
}
