package com.lgali.request.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import lombok.Data;

import static java.util.Collections.unmodifiableMap;

@Data
public class SupabaseUser implements UserDetails, Principal {

    private final List<GrantedAuthority> grantedAuthority = new ArrayList<>();
    private final Claims                 claims;
    private final String                 id;
    private final String                 role;
    private final String                 email;
    private final String                 phone;
    private final Map<String, String>    appMetadata;
    private final Map<String, String>    userMetadata;
    private final String                 fullName;
    private final String                 userName;
    private final String                 provider;
    private final String                 avatarUrl;
    private final String                 emailConfirmedAt;
    private final String                 confirmedAt;
    private final String                 lastSignInAt;
    private final String                 createdAt;
    private final String                 updatedAt;
    private final String                 password;

    public SupabaseUser(Claims claims, String accessToken) {
        this.claims = claims;
        this.password = accessToken;

        id = claims.get("sub", String.class);
        role = claims.get("role", String.class);

        grantedAuthority.clear();
        grantedAuthority.add(new SimpleGrantedAuthority(id));
        grantedAuthority.add(new SimpleGrantedAuthority(role));

        email = claims.get("email", String.class);
        phone = claims.get("phone", String.class);

        appMetadata = unmodifiableMap(claims.get("app_metadata", HashMap.class));
        userMetadata = (Map<String, String>) unmodifiableMap(claims.get("user_metadata", HashMap.class));

        if (userMetadata.get("avatar_url") != null) {avatarUrl = userMetadata.get("avatar_url");} else {
            avatarUrl = "/assets/svg/person-outline.svg";
        }
        fullName = userMetadata.get("full_name");
        if (userMetadata.get("user_name") != null) {userName = userMetadata.get("user_name");} else {userName = email;}

        emailConfirmedAt = userMetadata.get("email_confirmed_at");
        confirmedAt = userMetadata.get("confirmed_ad");
        lastSignInAt = userMetadata.get("last_sign_in_at");
        createdAt = userMetadata.get("created_at");
        updatedAt = userMetadata.get("updated_ad");

        provider = appMetadata.getOrDefault("provider", "");
    }

    public List<GrantedAuthority> getGrantedAuthority() {
        return grantedAuthority;
    }

    public Claims getClaims() {
        return claims;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthority;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
