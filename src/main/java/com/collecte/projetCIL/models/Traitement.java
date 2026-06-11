package com.collecte.projetCIL.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.collecte.projetCIL.enums.StatutTraitement;

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

    @Column(name = "nom", nullable = false)
    private String nom;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTraitement statut = StatutTraitement.EN_COURS;

    @ManyToOne
    @JoinColumn(name = "utilisateur_metier_id")
    private UtilisateurMetier utilisateurMetier;

    @ManyToOne
    @JoinColumn(name = "session_collecte_id")
    private SessionCollecte sessionCollecte;
}