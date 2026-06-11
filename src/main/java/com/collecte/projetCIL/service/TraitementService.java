package com.collecte.projetCIL.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.dto.request.DeclarationAutorisationRequest;
import com.collecte.projetCIL.dto.request.DeclarationCollecteSiteInternetRequest;
import com.collecte.projetCIL.dto.request.DeclarationNormaleRequest;
import com.collecte.projetCIL.dto.request.DeclarationVideoSurveillanceRequest;
import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.StatutDeclaration;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.enums.StatutTraitement;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.DeclarationAutorisation;
import com.collecte.projetCIL.models.DeclarationCollecteSiteInternet;
import com.collecte.projetCIL.models.DeclarationNormale;
import com.collecte.projetCIL.models.DeclarationSystemeVideoSurveillance;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.DeclarationAutorisationRepository;
import com.collecte.projetCIL.repository.DeclarationCollecteSiteInternetRepository;
import com.collecte.projetCIL.repository.DeclarationNormaleRepository;
import com.collecte.projetCIL.repository.DeclarationSystemeVideoSurveillanceRepository;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraitementService {

    private final TraitementRepository                        traitementRepository;
    private final SessionCollecteRepository                   sessionCollecteRepository;
    private final UtilisateurMetierRepository                 utilisateurMetierRepository;
    private final DPORepository                               dpoRepository;
    private final DeclarationNormaleRepository                declarationNormaleRepository;
    private final DeclarationCollecteSiteInternetRepository   declarationCollecteSiteInternetRepository;
    private final DeclarationSystemeVideoSurveillanceRepository declarationVideoSurveillanceRepository;
    private final DeclarationAutorisationRepository           declarationAutorisationRepository;
    private final JournalAuditService                         journalAuditService;
    private final NotificationService                         notificationService;

    // ------------------------------------------------------------------ //
    //  HELPERS PRIVÉS
    // ------------------------------------------------------------------ //

    /** Crée et sauvegarde l'entité Traitement à partir des champs communs. */
    private Traitement buildAndSaveTraitement(TraitementRequest request) {
        SessionCollecte session = sessionCollecteRepository.findById(request.getSessionCollecteId())
                .orElseThrow(() -> new RuntimeException("Session introuvable : " + request.getSessionCollecteId()));

        UtilisateurMetier utilisateurMetier = utilisateurMetierRepository.findById(request.getUtilisateurMetierId())
                .orElseThrow(() -> new RuntimeException("UtilisateurMetier introuvable : " + request.getUtilisateurMetierId()));

        Traitement traitement = new Traitement();
        traitement.setNom(request.getNom());
        traitement.setDepartment(request.getDepartment());
        traitement.setDescription(request.getDescription());
        traitement.setTexte(request.getTexte());
        traitement.setCertificationSecurite(request.getCertificationSecurite());
        traitement.setDureeConservation(request.getDureeConservation());
        traitement.setDateCreation(LocalDateTime.now());
        traitement.setDateFin(request.getDateFin());
        traitement.setNombreDonnee(0L);
        traitement.setStatut(StatutTraitement.EN_COURS);
        traitement.setSessionCollecte(session);
        traitement.setUtilisateurMetier(utilisateurMetier);

        return traitementRepository.save(traitement);
    }

    /** Pré-remplit les champs communs de la classe mère Declaration. */
    private void remplirDeclarationBase(com.collecte.projetCIL.models.Declaration decl,
                                        TraitementRequest request,
                                        Traitement traitement) {
        decl.setTraitement(traitement);
        decl.setStatut(StatutDeclaration.EN_ATTENTE);
        decl.setDateSoumission(LocalDate.now());

        decl.setSecteur(request.getSecteur());
        decl.setLieuStockage(request.getLieuStockage());
        decl.setDureeConservation(request.getDureeConservationDeclaration());
        decl.setDateMiseEnOeuvre(request.getDateMiseEnOeuvre());
        decl.setTransfertPaysEtranger(request.getTransfertEtranger());
        decl.setRecoursSousTraitant(request.getSousTraitance());
        decl.setCommunicationAutresOrganismes(request.getCommunicationTiers());

        decl.setNomRaisonSociale(request.getNomRaisonSociale());
        decl.setRccm(request.getRccm());
        decl.setSecteurActivite(request.getSecteurActivite());
        decl.setAdresse(request.getAdresse());
        decl.setBoitePostale(request.getBoitePostale());
        decl.setVille(request.getVille());
        decl.setTelephoneResponsable(request.getTelephone());
        decl.setAdresseEmailResponsable(request.getAdresseEmail());
        decl.setActivitePrincipale(request.getActivitePrincipale());

        SessionCollecte session = traitement.getSessionCollecte();
        DPO dpo = session.getDpo();
        decl.setDpo(dpo);
    }

    /** Enregistre l'audit et notifie le DPO. */
    private void auditEtNotifier(Traitement traitement, String typeDeclarationLabel) {
        UtilisateurMetier um = traitement.getUtilisateurMetier();
        SessionCollecte session = traitement.getSessionCollecte();
        DPO dpo = session.getDpo();

        journalAuditService.enregistrer(um, TypeAction.CREATION,
                ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        if (dpo != null) {
            notificationService.envoyer(dpo, TypeNotification.ALERTE,
                    "Un nouveau traitement « " + traitement.getDescription()
                    + " » (" + typeDeclarationLabel + ") a été créé par "
                    + um.getPrenom() + " " + um.getNom()
                    + " dans la session #" + session.getIdSession()
                    + ". Une déclaration a été pré-remplie et attend votre complétion.");
        }
    }

    // ------------------------------------------------------------------ //
    //  1. Créer traitement + DeclarationNormale
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationNormale(TraitementRequest request,
                                                           DeclarationNormaleRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationNormale decl = new DeclarationNormale();
        remplirDeclarationBase(decl, request, traitement);

        // Champs spécifiques DeclarationNormale
        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setNombrePersonnesConcernees(declRequest.getNombrePersonnesConcernees());
        decl.setDescriptionProcedureManuelle(declRequest.getDescriptionProcedureManuelle());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setCaracteristiquesSysteme(declRequest.getCaracteristiquesSysteme());
        decl.setPolitiqueAccesSystemes(declRequest.getPolitiqueAccesSystemes());
        decl.setModalitesDiffusionResultats(declRequest.getModalitesDiffusionResultats());
        decl.setProtocoleRecherche(declRequest.getProtocoleRecherche());
        decl.setDescriptionConnexionFichiers(declRequest.getDescriptionConnexionFichiers());
        decl.setMotifsInterconnexion(declRequest.getMotifsInterconnexion());
        decl.setIdentiteFichiersInterconnexion(declRequest.getIdentiteFichiersInterconnexion());

        DeclarationNormale saved = declarationNormaleRepository.save(decl);
        auditEtNotifier(traitement, "Déclaration Normale");

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  2. Créer traitement + DeclarationCollecteSiteInternet
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationCollecteSite(TraitementRequest request,
                                                                DeclarationCollecteSiteInternetRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationCollecteSiteInternet decl = new DeclarationCollecteSiteInternet();
        remplirDeclarationBase(decl, request, traitement);

        // Champs spécifiques DeclarationCollecteSiteInternet
        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setCaracteristiquesMainStructure(declRequest.getCaracteristiquesMainStructure());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setDonneesConnexion(declRequest.getDonneesConnexion());
        decl.setDescriptionDonneesConnexion(declRequest.getDescriptionDonneesConnexion());
        decl.setCookies(declRequest.getCookies());
        decl.setDescriptionCookies(declRequest.getDescriptionCookies());
        decl.setDureeConservationCookies(declRequest.getDureeConservationCookies());
        decl.setTelechargementTraitement(declRequest.getTelechargementTraitement());

        DeclarationCollecteSiteInternet saved = declarationCollecteSiteInternetRepository.save(decl);
        auditEtNotifier(traitement, "Collecte Site Internet");

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  3. Créer traitement + DeclarationSystemeVideoSurveillance
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationVideoSurveillance(TraitementRequest request,
                                                                     DeclarationVideoSurveillanceRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationSystemeVideoSurveillance decl = new DeclarationSystemeVideoSurveillance();
        remplirDeclarationBase(decl, request, traitement);

        // Champs spécifiques DeclarationSystemeVideoSurveillance
        decl.setFinalites(declRequest.getFinalites());
        decl.setAdresseInstallation(declRequest.getAdresseInstallation());
        decl.setNatureEnvironnement(declRequest.getNatureEnvironnement());
        decl.setEmplacementCameras(declRequest.getEmplacementCameras());
        decl.setNombreTotalCameras(declRequest.getNombreTotalCameras());
        decl.setModeleDispositif(declRequest.getModeleDispositif());
        decl.setVisualisationTempsReel(declRequest.getVisualisationTempsReel());
        decl.setModeTransfert(declRequest.getModeTransfert());
        decl.setSonDeSon(declRequest.getSonDeSon());
        decl.setTypeEnregistrement(declRequest.getTypeEnregistrement());
        decl.setNatureEnregistrement(declRequest.getNatureEnregistrement());
        decl.setLiaisonReseau(declRequest.getLiaisonReseau());
        decl.setUtilisationSystemesExperts(declRequest.getUtilisationSystemesExperts());
        decl.setDescriptionSystemesExperts(declRequest.getDescriptionSystemesExperts());
        decl.setFonctionnalitesTraitement(declRequest.getFonctionnalitesTraitement());
        decl.setAccesImagesDistance(declRequest.getAccesImagesDistance());
        decl.setAccesPhysique(declRequest.getAccesPhysique());
        decl.setAccesLogique(declRequest.getAccesLogique());
        decl.setMesuresSuppression(declRequest.getMesuresSuppression());
        decl.setAttribute(declRequest.getAttribute());
        decl.setLocalisationPictogrammes(declRequest.getLocalisationPictogrammes());

        DeclarationSystemeVideoSurveillance saved = declarationVideoSurveillanceRepository.save(decl);
        auditEtNotifier(traitement, "Vidéo Surveillance");

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  4. Créer traitement + DeclarationAutorisation
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationAutorisation(TraitementRequest request,
                                                                DeclarationAutorisationRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationAutorisation decl = new DeclarationAutorisation();
        remplirDeclarationBase(decl, request, traitement);

        // Champs spécifiques DeclarationAutorisation
        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setNombrePersonnesConcernees(declRequest.getNombrePersonnesConcernees());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setFonctionnalitesSysteme(declRequest.getFonctionnalitesSysteme());
        decl.setCertificationSecurite(declRequest.getCertificationSecurite());
        decl.setPolitiqueAccesSystemes(declRequest.getPolitiqueAccesSystemes());
        decl.setDescriptionFichier(declRequest.getDescriptionFichier());
        decl.setModeTransfert(declRequest.getModeTransfert());
        decl.setTraitementDonneesSante(declRequest.getTraitementDonneesSante());
        decl.setProfessionalSante(declRequest.getProfessionalSante());
        decl.setDestinataireAdresse(declRequest.getDestinataireAdresse());
        decl.setTexteJuridiqueCommunication(declRequest.getTexteJuridiqueCommunication());
        decl.setModalitesDiffusionResultats(declRequest.getModalitesDiffusionResultats());
        decl.setDestinataireCie(declRequest.getDestinataireCie());
        decl.setConnexionFichiers(declRequest.getConnexionFichiers());
        decl.setCategoriesDonneesInterconnexion(declRequest.getCategoriesDonneesInterconnexion());
        decl.setDureeInterconnexion(declRequest.getDureeInterconnexion());
        decl.setIdentiteFichiersInterconnexion(declRequest.getIdentiteFichiersInterconnexion());
        decl.setTransfertPaysEtranger(declRequest.getTransfertPaysEtranger());
        decl.setRecoursSousTraitant(declRequest.getRecoursSousTraitant());
        decl.setRolesSousTraitants(declRequest.getRolesSousTraitants());
        decl.setCategoriesPersonnesAcces(declRequest.getCategoriesPersonnesAcces());
        decl.setPolitiqueAccesBatiments(declRequest.getPolitiqueAccesBatiments());
        decl.setMesuresSecurite(declRequest.getMesuresSecurite());
        decl.setDescriptionSensibilisation(declRequest.getDescriptionSensibilisation());
        decl.setPaysDestinationProtectionDonnees(declRequest.getPaysDestinationProtectionDonnees());
        decl.setDescriptionFichierTransfert(declRequest.getDescriptionFichierTransfert());
        decl.setNombrePersonnesTransfert(declRequest.getNombrePersonnesTransfert());
        decl.setCategoriesDonneesTransfert(declRequest.getCategoriesDonneesTransfert());
        decl.setFondementJuridique(declRequest.getFondementJuridique());
        decl.setConsentementPersonnesConcernees(declRequest.getConsentementPersonnesConcernees());
        decl.setMethodeRecueilConsentement(declRequest.getMethodeRecueilConsentement());
        decl.setMesuresSecuriteTransfert(declRequest.getMesuresSecuriteTransfert());
        decl.setLieuStockage(declRequest.getLieuStockage());
        decl.setCommunicationAutresOrganismes(declRequest.getCommunicationAutresOrganismes());
        decl.setDestinataireNomPrenom(declRequest.getDestinataireNomPrenom());
        decl.setDureeConservationSante(declRequest.getDureeConservationSante());
        decl.setOrigineDonnees(declRequest.getOrigineDonnees());

        DeclarationAutorisation saved = declarationAutorisationRepository.save(decl);
        auditEtNotifier(traitement, "Autorisation");

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements d'une session
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParSession(Long sessionId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> t.getSessionCollecte() != null
                        && t.getSessionCollecte().getIdSession().equals(sessionId))
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Obtenir un traitement par ID
    // ------------------------------------------------------------------ //
    public TraitementResponse getTraitementById(Long id) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Incrémenter le compteur de données
    // ------------------------------------------------------------------ //
    public void incrementerNombreDonnee(Long traitementId, long quantite) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));
        long actuel = traitement.getNombreDonnee() != null ? traitement.getNombreDonnee() : 0L;
        traitement.setNombreDonnee(actuel + quantite);
        traitementRepository.save(traitement);
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private TraitementResponse toResponse(Traitement t, Long declarationId) {
        String nomMetier = (t.getUtilisateurMetier() != null)
                ? t.getUtilisateurMetier().getPrenom() + " " + t.getUtilisateurMetier().getNom()
                : null;
        return new TraitementResponse(
                t.getIdTraitement(),
                t.getDepartment(),
                t.getDescription(),
                t.getTexte(),
                t.getCertificationSecurite(),
                t.getDureeConservation(),
                t.getDateCreation(),
                t.getDateFin(),
                t.getNombreDonnee(),
                t.getSessionCollecte() != null ? t.getSessionCollecte().getIdSession() : null,
                t.getUtilisateurMetier() != null ? t.getUtilisateurMetier().getId() : null,
                nomMetier,
                declarationId,
                t.getStatut()
        );
    }

    // ------------------------------------------------------------------ //
    //  Changer le statut d'un traitement
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse updateStatut(Long id, StatutTraitement nouveauStatut) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
        traitement.setStatut(nouveauStatut);
        traitementRepository.save(traitement);
        return toResponse(traitement, null);
    }

}