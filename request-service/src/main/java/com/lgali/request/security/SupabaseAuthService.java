package com.lgali.request.security;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class SupabaseAuthService {

    @Value("${supabase.jwt_secret}")
    String jwtSecret;

    public SupabaseUser user(String accessToken) throws AuthenticationException {
        String encoded = Base64.getEncoder().encodeToString(jwtSecret.getBytes());

        try {
            Jwt<JwsHeader, Claims> parse = Jwts.parserBuilder().setSigningKey(encoded).
                                               requireAudience("authenticated").
                                               build().parseClaimsJws(accessToken);
            return new SupabaseUser(parse.getBody(), accessToken);
        } catch (ExpiredJwtException |
                 SignatureException |
                 MalformedJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException expiredJwtException) {
            return null;
        }

    }
}
