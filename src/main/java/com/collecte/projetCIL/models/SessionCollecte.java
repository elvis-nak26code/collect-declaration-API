package com.collecte.projetCIL.models;

import java.time.LocalDateTime;
import java.util.List;

import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.enums.TypeCollecte;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_collecte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCollecte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_session")
    private Long idSession;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "nom_session")
    private String nomSession;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_session")
    private StatutSession statutSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_collecte")
    private TypeCollecte typeCollecte;

    private String lieu;
    private String description;

    @ManyToOne
    @JoinColumn(name = "dpo_id")
    private DPO dpo;

    @OneToMany(mappedBy = "sessionCollecte", cascade = CascadeType.ALL)
    private List<Traitement> traitements;
}