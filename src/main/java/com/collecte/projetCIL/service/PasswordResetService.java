package com.collecte.projetCIL.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.PasswordResetToken;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.PasswordResetTokenRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int DUREE_VALIDITE_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Étape 1 : la personne saisit son email. On génère un jeton à usage
     * unique, valable {@value #DUREE_VALIDITE_MINUTES} minutes, et on lui
     * envoie un lien de réinitialisation par email.
     */
    @Transactional
    public void demanderReinitialisation(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Veuillez saisir votre adresse email.");
        }
        String emailNormalise = email.trim().toLowerCase();

        boolean compteExiste = utilisateurRepository.findByEmail(emailNormalise).isPresent()
                || administrateurRepository.findByEmail(emailNormalise).isPresent();

        if (!compteExiste) {
            throw new RuntimeException(
                    "Aucun compte n'est associé à l'adresse email \"" + email + "\".");
        }

        String tokenBrut = genererTokenAleatoire();
        String tokenHash = hacher(tokenBrut);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(emailNormalise);
        resetToken.setTokenHash(tokenHash);
        resetToken.setDateCreation(LocalDateTime.now());
        resetToken.setDateExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES));
        resetToken.setUtilise(false);
        passwordResetTokenRepository.save(resetToken);

        String lien = frontendUrl + "/reinitialiser-mot-de-passe?token=" + tokenBrut;
        String contenu = "Bonjour,\n\n"
                + "Une demande de réinitialisation de mot de passe a été effectuée pour votre compte "
                + "sur la plateforme de collecte de données SOFITEX.\n\n"
                + "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe "
                + "(valable " + DUREE_VALIDITE_MINUTES + " minutes) :\n"
                + lien + "\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email : "
                + "votre mot de passe actuel restera inchangé.\n\n"
                + "— SOFITEX, plateforme de collecte de données";

        emailService.envoyer(emailNormalise, "Réinitialisation de votre mot de passe — SOFITEX", contenu);
    }

    /**
     * Étape 2 : la personne clique sur le lien reçu et choisit un nouveau
     * mot de passe. On vérifie que le jeton est valide, non expiré et non
     * déjà utilisé, puis on met à jour le mot de passe du bon compte
     * (Administrateur ou Utilisateur / une de ses sous-classes).
     */
    @Transactional
    public void reinitialiserMotDePasse(String tokenBrut, String nouveauMotDePasse) {
        if (tokenBrut == null || tokenBrut.isBlank()) {
            throw new RuntimeException("Le lien de réinitialisation est invalide.");
        }
        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
        }

        String tokenHash = hacher(tokenBrut);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException(
                        "Ce lien de réinitialisation est invalide ou a déjà été utilisé. "
                        + "Veuillez refaire une demande de mot de passe oublié."));

        if (resetToken.isUtilise()) {
            throw new RuntimeException(
                    "Ce lien de réinitialisation a déjà été utilisé. Veuillez refaire une demande.");
        }
        if (resetToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Ce lien de réinitialisation a expiré (validité " + DUREE_VALIDITE_MINUTES + " minutes). "
                    + "Veuillez refaire une demande de mot de passe oublié.");
        }

        String email = resetToken.getEmail();
        String motDePasseEncode = passwordEncoder.encode(nouveauMotDePasse);

        var adminOpt = administrateurRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrateur admin = adminOpt.get();
            admin.setMotDePasse(motDePasseEncode);
            administrateurRepository.save(admin);
        } else {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(
                            "Le compte associé à ce lien n'existe plus."));
            utilisateur.setMotDePasse(motDePasseEncode);
            utilisateurRepository.save(utilisateur);
        }

        resetToken.setUtilise(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String genererTokenAleatoire() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hacher(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valeur.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur interne lors de la génération du jeton de sécurité.");
        }
    }
}
