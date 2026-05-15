package com.collecte.projetCIL.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "dpo")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DPO extends Utilisateur {

    private String organisme;

    @Column(name = "adresse_professionnelle")
    private String adresseProfessionnelle;

    @Column(name = "date_nomination")
    private LocalDateTime dateNomination;

    // 1 DPO -> 0..* Declaration (soumet)
    @OneToMany(mappedBy = "dpo", cascade = CascadeType.ALL)
    private List<Declaration> declarations;

    // 1 DPO -> 0..* SessionCollecte (initie)
    @OneToMany(mappedBy = "dpo", cascade = CascadeType.ALL)
    private List<SessionCollecte> sessionsCollecte;
}
