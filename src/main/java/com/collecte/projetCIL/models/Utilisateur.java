package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.Permission;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
