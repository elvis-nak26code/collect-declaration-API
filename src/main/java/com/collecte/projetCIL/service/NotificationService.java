package com.collecte.projetCIL.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.collecte.projetCIL.dto.response.NotificationResponse;
import com.collecte.projetCIL.enums.StatutNotification;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.Notification;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    // ------------------------------------------------------------------ //
    //  Créer et persister une notification (usage interne)
    //  + envoi automatique d'un email au destinataire (si email connu)
    // ------------------------------------------------------------------ //
    public void envoyer(Utilisateur destinataire, TypeNotification type, String contenu) {
        Notification notif = new Notification();
        notif.setDateEnvoi(LocalDate.now());
        notif.setTypeNotification(type);
        notif.setContenu(contenu);
        notif.setStatut(StatutNotification.NON_LUE);
        notif.setUtilisateur(destinataire);
        notificationRepository.save(notif);

        envoyerEmailNotification(destinataire, type, contenu);
    }

    /**
     * Traduit une notification en email. Ne bloque jamais le flux appelant :
     * une erreur d'envoi est uniquement journalisée par EmailService (mode fallback).
     */
    private void envoyerEmailNotification(Utilisateur destinataire, TypeNotification type, String contenu) {
        if (destinataire == null || destinataire.getEmail() == null || destinataire.getEmail().isBlank()) {
            return;
        }
        String sujet = switch (type) {
            case ALERTE                 -> "Alerte — action requise";
            case RAPPEL                 -> "Rappel";
            case CONFIRMATION           -> "Confirmation";
            case RELANCE                -> "Relance";
            case DEMANDE_MODIFICATION   -> "Demande de modification de vos données";
            case DEMANDE_SUPPRESSION    -> "Demande de suppression de vos données";
            case PLAINTE                -> "Suivi de votre plainte";
        };
        String prenomNom = ((destinataire.getPrenom() != null ? destinataire.getPrenom() : "")
                + " " + (destinataire.getNom() != null ? destinataire.getNom() : "")).trim();
        String corps = "Bonjour " + (prenomNom.isBlank() ? "" : prenomNom) + ",\n\n"
                + contenu + "\n\n"
                + "Vous pouvez consulter le détail de cette notification depuis votre espace.\n\n"
                + "Ceci est un message automatique, merci de ne pas y répondre directement.";
        emailService.envoyer(destinataire.getEmail(), sujet, corps);
    }

    // ------------------------------------------------------------------ //
    //  Lister les notifications d'un utilisateur
    // ------------------------------------------------------------------ //
    public List<NotificationResponse> listerParUtilisateur(Long utilisateurId) {
        return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(utilisateurId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Lister uniquement les non lues
    // ------------------------------------------------------------------ //
    public List<NotificationResponse> listerNonLues(Long utilisateurId) {
        return notificationRepository.findNonLuesByUtilisateurId(utilisateurId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Marquer une notification comme lue
    // ------------------------------------------------------------------ //
    public NotificationResponse marquerCommeLue(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable : " + notificationId));
        notif.setStatut(StatutNotification.LUE);
        return toResponse(notificationRepository.save(notif));
    }

    // ------------------------------------------------------------------ //
    //  Marquer toutes les notifications d'un utilisateur comme lues
    // ------------------------------------------------------------------ //
    public void marquerToutesCommeLues(Long utilisateurId) {
        List<Notification> nonLues = notificationRepository.findNonLuesByUtilisateurId(utilisateurId);
        nonLues.forEach(n -> n.setStatut(StatutNotification.LUE));
        notificationRepository.saveAll(nonLues);
    }

    // ------------------------------------------------------------------ //
    //  Mapper
    // ------------------------------------------------------------------ //
    private NotificationResponse toResponse(Notification n) {
        String nomPrenom = null;
        if (n.getUtilisateur() != null) {
            nomPrenom = n.getUtilisateur().getPrenom() + " " + n.getUtilisateur().getNom();
        }
        return new NotificationResponse(
                n.getIdNotification(),
                n.getDateEnvoi(),
                n.getTypeNotification(),
                n.getContenu(),
                n.getStatut(),
                n.getDateEcheance(),
                n.getUtilisateur() != null ? n.getUtilisateur().getId() : null,
                nomPrenom
        );
    }
}