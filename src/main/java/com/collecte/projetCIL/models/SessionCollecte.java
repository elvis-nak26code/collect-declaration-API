package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.enums.TypeCollecte;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    // ManyToOne DPO (initie)
    @ManyToOne
    @JoinColumn(name = "dpo_id")
    private DPO dpo;

    // 1 SessionCollecte -> 0..* Traitement
    @OneToMany(mappedBy = "sessionCollecte", cascade = CascadeType.ALL)
    private List<Traitement> traitements;
}
