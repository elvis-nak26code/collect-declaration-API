package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutNotification;
import com.collecte.projetCIL.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification", columnDefinition = "bigserial")
    private Long idNotification;

    @Column(name = "date_envoi")
    private LocalDate dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification")
    private TypeNotification typeNotification;

    private String contenu;

    @Enumerated(EnumType.STRING)
    private StatutNotification statut;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}