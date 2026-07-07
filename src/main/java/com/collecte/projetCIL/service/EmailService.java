package com.collecte.projetCIL.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Envoi d'emails transactionnels (réinitialisation de mot de passe, etc.).
 *
 * Tant que le SMTP n'est pas configuré (spring.mail.username/password vides
 * dans application.properties) ou si l'envoi échoue pour une autre raison,
 * le message n'est jamais perdu : il est simplement affiché dans les logs du
 * serveur ("mode fallback"), ce qui permet de continuer à développer/tester
 * la fonctionnalité sans compte SMTP réel.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void envoyer(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);
            mailSender.send(message);
            log.info("Email envoyé à {} — sujet : {}", destinataire, sujet);
        } catch (Exception e) {
            // ── Mode fallback : SMTP non configuré ou indisponible ──────────
            log.warn("⚠️ Envoi d'email impossible (SMTP non configuré ou erreur : {}). "
                    + "Contenu affiché ci-dessous à des fins de développement/test.", e.getMessage());
            log.warn("──────── EMAIL (fallback) ────────\nÀ : {}\nSujet : {}\n{}\n───────────────────────────────────",
                    destinataire, sujet, contenu);
        }
    }
}
