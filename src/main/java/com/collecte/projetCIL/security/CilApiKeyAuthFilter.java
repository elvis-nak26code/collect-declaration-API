package com.collecte.projetCIL.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.collecte.projetCIL.models.CleApiCil;
import com.collecte.projetCIL.service.CleApiCilService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Authentifie les appels externes de la CIL via le header "X-API-KEY",
 * SANS login ni JWT. Ne s'applique qu'aux routes /api/cil-externe/**.
 *
 * Si la clé est absente/invalide, ce filtre ne fait rien : c'est
 * SecurityConfig (hasAuthority("ROLE_CIL_API")) qui refusera ensuite
 * l'accès avec un 401/403, exactement comme pour un JWT absent/invalide.
 */
@Component
@RequiredArgsConstructor
public class CilApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";
    private static final String PREFIXE_ROUTE = "/api/cil-externe";

    private final CleApiCilService cleApiCilService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith(PREFIXE_ROUTE)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String cleEnClair = request.getHeader(HEADER);
            Optional<CleApiCil> cleValide = cleApiCilService.verifier(cleEnClair);

            if (cleValide.isPresent()) {
                CleApiCil c = cleValide.get();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                c,                         // principal = la clé API elle-même (aucune fiche CIL en base)
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_CIL_API")));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                cleApiCilService.marquerUtilisee(c);
            }
        }

        filterChain.doFilter(request, response);
    }
}