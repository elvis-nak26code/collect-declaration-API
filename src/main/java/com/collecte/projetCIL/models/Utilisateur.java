package com.collecte.projetCIL.models;

import java.time.LocalDateTime;
import java.util.List;

import com.collecte.projetCIL.enums.Permission;
import com.collecte.projetCIL.enums.StatutUtilisateur;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_utilisateur")
    private StatutUtilisateur statutUtilisateur;

    @Enumerated(EnumType.STRING)
    private Permission permission;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "dernier_acces")          // ← nouveau champ pour lastLogin
    private LocalDateTime dernierAcces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personne_id")
    private Personne personne;

    // 1 Utilisateur -> 0..1 DemandeAcces
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private DemandeAcces demandeAcces;

    // 1 Utilisateur -> 0..* Notification
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    // 1 Utilisateur -> 0..* JournalAudit
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<JournalAudit> journalAudits;
}