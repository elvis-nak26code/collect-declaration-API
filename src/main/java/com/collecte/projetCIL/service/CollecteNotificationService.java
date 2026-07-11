package com.collecte.projetCIL.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.repository.UsagerRepository;

import lombok.RequiredArgsConstructor;

/**
 * Envoie un email d'information à une personne dès qu'une donnée la
 * concernant a été collectée dans le système (traitement OU entrepôt),
 * conformément à l'obligation d'information de la personne concernée.
 *
 * Si la personne ne possède pas encore de compte usager (même email dans la
 * table utilisateur/usager), l'email l'invite en plus à télécharger
 * l'application pour pouvoir exercer ses droits (accès, rectification,
 * opposition, suppression). Si elle a déjà un compte, ce rappel est omis.
 */
@Service
@RequiredArgsConstructor
public class CollecteNotificationService {

    private final EmailService emailService;
    private final UsagerRepository usagerRepository;

    @Value("${app.mobile.download.url:https://cil-app.example.com/telecharger}")
    private String lienTelechargementApp;

    /**
     * @param personne la personne concernée par la donnée collectée (peut être null : ignoré silencieusement)
     */
    public void notifierCollecte(Personne personne) {
        if (personne == null) return;
        String email = personne.getEmail();
        if (email == null || email.isBlank()) return;

        boolean possedeDejaCompte;
        try {
            possedeDejaCompte = usagerRepository.existsByEmail(email);
        } catch (Exception e) {
            possedeDejaCompte = false;
        }

        String prenomNom = ((personne.getPrenom() != null ? personne.getPrenom() : "")
                + " " + (personne.getNom() != null ? personne.getNom() : "")).trim();

        String sujet = "Vos données personnelles ont été collectées";

        StringBuilder corps = new StringBuilder();
        corps.append("Bonjour").append(prenomNom.isBlank() ? "" : " " + prenomNom).append(",\n\n");
        corps.append("Nous vous informons qu'une ou plusieurs données personnelles vous concernant ")
             .append("viennent d'être collectées et enregistrées dans notre système, ")
             .append("conformément à la réglementation en vigueur sur la protection des données personnelles.\n\n");

        if (possedeDejaCompte) {
            corps.append("Vous pouvez à tout moment consulter ces données et exercer vos droits ")
                 .append("(accès, rectification, opposition, suppression) depuis l'application que vous avez déjà installée.\n\n");
        } else {
            corps.append("Pour consulter ces données et exercer vos droits ")
                 .append("(accès, rectification, opposition, suppression), veuillez télécharger notre application :\n")
                 .append(lienTelechargementApp).append("\n\n");
        }

        corps.append("Ceci est un message automatique, merci de ne pas y répondre directement.");

        emailService.envoyer(email, sujet, corps.toString());
    }
}
