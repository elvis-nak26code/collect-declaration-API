package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutSession;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "traitement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Traitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_traitement")
    private Long idTraitement;

    private String department;
    private String description;
    private String texte;

    @Column(name = "certification_securite")
    private String certificationSecurite;

    @Column(name = "duree_conservation")
    private Integer dureeConservation;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "nombre_donnee")
    private Long nombreDonnee;

    // ManyToOne UtilisateurMetier (gère)
    @ManyToOne
    @JoinColumn(name = "utilisateur_metier_id")
    private UtilisateurMetier utilisateurMetier;

    // ManyToOne SessionCollecte (regroupe)
    @ManyToOne
    @JoinColumn(name = "session_collecte_id")
    private SessionCollecte sessionCollecte;
}
