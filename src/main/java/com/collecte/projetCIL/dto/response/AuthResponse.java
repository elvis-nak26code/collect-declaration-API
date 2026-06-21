package com.collecte.projetCIL.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type;
    private String email;
    private String role;
    private Long id;

    public AuthResponse(String token, String email, String role, Long id) {
        this.token = token;
        this.type = "Bearer";
        this.email = email;
        this.role = role;
        this.id = id;
    }
}