package com.collecte.projetCIL.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.collecte.projetCIL.dto.request.LoginRequest;
import com.collecte.projetCIL.dto.response.AuthResponse;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.ConnexionAppareil;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.ConnexionAppareilRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import com.collecte.projetCIL.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final ConnexionAppareilRepository connexionAppareilRepository;
    private final EmailService emailService;

    /**
     * Surcharge historique (sans info d'appareil) : conservée pour ne rien
     * casser côté appelants existants qui n'auraient pas encore été mis à jour.
     * Aucune alerte de sécurité n'est envoyée dans ce cas (pas d'IP/UA connus).
     */
    public AuthResponse login(LoginRequest request) {
        return login(request, null, null);
    }

    /**
     * @param ipAddress adresse IP du client au moment de la connexion (peut être null)
     * @param userAgent user-agent du navigateur/app au moment de la connexion (peut être null)
     */
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getMotDePasse())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN");

        // UserDetails ne porte que l'email + les rôles : on va chercher
        // l'id / nom / prénom réels selon le type de compte, pour pouvoir
        // les embarquer dans le JWT (le frontend en a besoin).
        Long id;
        String nom;
        String prenom;

        if ("ROLE_ADMINISTRATEUR".equals(role)) {
            Administrateur admin = administrateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Administrateur introuvable : " + email));
            id = admin.getId();
            nom = admin.getNom();
            prenom = admin.getPrenom();
        } else {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
            id = u.getId();
            nom = u.getNom();
            prenom = u.getPrenom();
        }

        String token = jwtUtils.generateToken(email, role, id, nom, prenom);

        // ── Détection nouvel appareil / connexion suspecte ───────────────────
        // Ne doit JAMAIS faire échouer la connexion : toute erreur ici est
        // uniquement journalisée, le login reste fonctionnel dans tous les cas.
        try {
            verifierNouvelAppareil(email, nom, prenom, ipAddress, userAgent);
        } catch (Exception e) {
            log.warn("Vérification de sécurité (nouvel appareil) impossible pour {} : {}", email, e.getMessage());
        }

        return new AuthResponse(token, email, role, id);
    }

    /**
     * Compare l'IP + user-agent de la connexion en cours à l'historique connu
     * pour ce compte. Si la combinaison est inconnue, une alerte de sécurité
     * est envoyée par email et l'empreinte est enregistrée pour la prochaine fois.
     * Si elle est déjà connue, seule la date de dernière connexion est mise à jour.
     */
    private void verifierNouvelAppareil(String email, String nom, String prenom,
                                        String ipAddress, String userAgent) {
        // Pas d'info exploitable (ex: appel interne sans requête HTTP) : on ignore.
        if ((ipAddress == null || ipAddress.isBlank()) && (userAgent == null || userAgent.isBlank())) {
            return;
        }
        String ip = ipAddress != null ? ipAddress : "inconnue";
        String ua = userAgent != null ? userAgent : "inconnu";

        var existant = connexionAppareilRepository.findByEmailAndIpAddressAndUserAgent(email, ip, ua);

        if (existant.isPresent()) {
            ConnexionAppareil c = existant.get();
            c.setDerniereConnexion(LocalDateTime.now());
            connexionAppareilRepository.save(c);
            return;
        }

        boolean premiereConnexionCompte = connexionAppareilRepository.findByEmail(email).isEmpty();

        // Nouvelle combinaison IP + appareil pour ce compte -> enregistrement.
        ConnexionAppareil nouvelle = new ConnexionAppareil();
        nouvelle.setEmail(email);
        nouvelle.setIpAddress(ip);
        nouvelle.setUserAgent(ua);
        nouvelle.setPremiereConnexion(LocalDateTime.now());
        nouvelle.setDerniereConnexion(LocalDateTime.now());
        connexionAppareilRepository.save(nouvelle);

        if (premiereConnexionCompte) {
            // Tout premier login connu pour ce compte : rien à comparer, pas d'alerte.
            return;
        }

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        String sujet = "Alerte de sécurité — nouvelle connexion détectée";
        String contenu = "Bonjour " + (prenom != null ? prenom : "") + " " + (nom != null ? nom : "") + ",\n\n"
                + "Une connexion à votre compte a été détectée depuis un appareil ou un réseau non reconnu :\n"
                + "  • Date : " + date + "\n"
                + "  • Adresse IP : " + ip + "\n"
                + "  • Appareil/navigateur : " + ua + "\n\n"
                + "Si c'est bien vous, aucune action n'est nécessaire.\n"
                + "Si vous ne reconnaissez pas cette connexion, changez votre mot de passe immédiatement "
                + "et contactez votre administrateur.\n\n"
                + "Ceci est un message automatique, merci de ne pas y répondre directement.";

        emailService.envoyer(email, sujet, contenu);
    }
}
