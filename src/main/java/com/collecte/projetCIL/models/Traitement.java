package com.collecte.projetCIL.models;

import java.time.LocalDateTime;

import com.collecte.projetCIL.enums.StatutTraitement;

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

    /**
     * Indique si le UtilisateurMetier a explicitement envoyé ce traitement
     * (et sa déclaration pré-remplie) au DPO pour traitement.
     * Tant que false, le DPO ne doit pas voir ce traitement.
     */
    @Column(name = "envoye_au_dpo", nullable = false)
    private Boolean envoyeAuDpo = false;

    /** Date à laquelle l'envoi au DPO a été effectué. */
    @Column(name = "date_envoi_dpo")
    private LocalDateTime dateEnvoiDpo;

    @ManyToOne
    @JoinColumn(name = "utilisateur_metier_id")
    private UtilisateurMetier utilisateurMetier;

    /**
     * Session de collecte associée (optionnelle). Un traitement peut être créé
     * sans session, puis lié à une session ultérieurement.
     */
    @ManyToOne
    @JoinColumn(name = "session_collecte_id", nullable = true)
    private SessionCollecte sessionCollecte;
}