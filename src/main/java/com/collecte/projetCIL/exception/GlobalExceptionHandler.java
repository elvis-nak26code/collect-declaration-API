package com.collecte.projetCIL.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Gestionnaire d'erreurs global.
 *
 * Sans ce composant, Spring Boot masque par défaut le message des exceptions
 * dans la réponse JSON (server.error.include-message = never) : le frontend
 * ne reçoit alors qu'un statut HTTP brut (404, 403, 500…) sans explication.
 *
 * Toutes les erreurs passent désormais par ici et renvoient un corps JSON
 * cohérent : { "message": "...", "status": ..., "timestamp": "...", "path": "..." }
 * avec un message clair, en français, compréhensible par l'utilisateur final.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Règles métier / données introuvables — levées via RuntimeException ──
    // (ex: "Usager introuvable : ...", "Doublon détecté : ...", etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("introuvable") || lower.contains("inexistant")
                    || (lower.contains("aucun") && lower.contains("trouv"))
                    || (lower.contains("n'existe") )) {
                status = HttpStatus.NOT_FOUND;
            } else if (lower.contains("doublon") || lower.contains("existe déjà") || lower.contains("déjà")) {
                status = HttpStatus.CONFLICT;
            }
        } else {
            message = "Une erreur est survenue lors du traitement de votre demande.";
        }

        return buildResponse(status, message, req);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : "La ressource demandée est introuvable.", req);
    }

    // ── Authentification ────────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, "Ce compte est désactivé. Contactez un administrateur.", req);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(LockedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, "Ce compte est verrouillé. Contactez un administrateur.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentification requise ou invalide. Veuillez vous reconnecter.", req);
    }

    // ── Autorisation (403) ───────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN,
                "Vous n'avez pas les autorisations nécessaires pour effectuer cette action.", req);
    }

    // ── Validation des champs (@Valid) ──────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .collect(Collectors.joining(" — "));
        String message = details.isBlank()
                ? "Certains champs du formulaire sont invalides."
                : "Formulaire invalide : " + details;
        return buildResponse(HttpStatus.BAD_REQUEST, message, req);
    }

    // ── Ressource / route introuvable (404 générique) ───────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND,
                "La ressource ou la page demandée n'existe pas (" + ex.getRequestURL() + ").", req);
    }

    // ── Fichiers ─────────────────────────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                "Le fichier envoyé est trop volumineux. Veuillez réduire sa taille et réessayer.", req);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPart(MissingServletRequestPartException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Le fichier attendu (\"" + ex.getRequestPartName() + "\") est manquant dans la requête.", req);
    }

    // ── Contraintes base de données (ex: doublon sur une colonne unique) ────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.CONFLICT,
                "Cette opération viole une contrainte de données (doublon ou valeur invalide). "
                + "Vérifiez que l'information n'existe pas déjà.", req);
    }

    // ── Filet de sécurité : toute autre exception non prévue ────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue. Veuillez réessayer ou contacter l'administrateur si le problème persiste.", req);
    }

    // ── Construction de la réponse JSON uniforme ────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", req != null ? req.getRequestURI() : null);
        return ResponseEntity.status(status).body(body);
    }
}
