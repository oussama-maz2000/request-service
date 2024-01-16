package com.lgali.request.security;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

@Order(1)
@Slf4j
public class JwtAuthorizeFilter extends BasicAuthenticationFilter {

    public static final String              TOKEN_PREFIX  = "Bearer ";
    public static final String              HEADER_STRING = "Authorization";
    private final       SupabaseAuthService supabaseAuthService;

    public JwtAuthorizeFilter(final AuthenticationManager authenticationManager,
                              final SupabaseAuthService supabaseAuthService) {
        super(authenticationManager);
        this.supabaseAuthService = supabaseAuthService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(HEADER_STRING);

        log.info("header used {} ", header);
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    // Reads the JWT from the Authorization header, and then uses JWT to validate the token
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

        String token = request.getHeader(HEADER_STRING).split(TOKEN_PREFIX)[1];
        if (token != null) {
            // parse the token.
            SupabaseUser user = supabaseAuthService.user(token);

            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, user.getPassword(), new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
