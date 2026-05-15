package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "utilisateur_metier")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurMetier extends Utilisateur {

    private String fonction;

    @Column(name = "date_nomination")
    private LocalDateTime dateNomination;

    private String telephone;

    // 1 UtilisateurMetier -> 0..* Traitement (gère)
    @OneToMany(mappedBy = "utilisateurMetier", cascade = CascadeType.ALL)
    private List<Traitement> traitements;

    // 1 UtilisateurMetier -> 0..* Demande (traite)
    @OneToMany(mappedBy = "utilisateurMetier", cascade = CascadeType.ALL)
    private List<Demande> demandes;
}
