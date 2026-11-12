package com.healthybites.security;

import com.healthybites.config.properties.JwtProperties;
import com.healthybites.exception.RoleNotFoundException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
public class TokenProvider {

    //private final UsuarioRepository userRepository;

    private final JwtProperties jwtProperties;

    private SecretKey key;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret().trim()));
        jwtParser = Jwts
                .parser()
                .verifyWith(key)
                .build();
    }

    // Generar tokens JWT
    public String createAccessToken(Authentication authentication) {
        // Obtener el email del usuario autenticado desde el principal
        String correo = authentication.getName();

        /* Buscar el usuario en la base de datos usando el email
        Usuario usuario = new Usuario()*/

        // Obtener el rol del usuario
        String role = authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow(RoleNotFoundException::new)
                .getAuthority();

        // Crear el token JWT con solo el rol y el sujeto
        return Jwts
                .builder()
                .subject(authentication.getName())  // El sujeto será el email o el nombre de usuario
                .claim("role", role)  // Solo se incluye el rol como claim
                .signWith(key, Jwts.SIG.HS512)
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.validity().toMillis()))
                .compact();
    }

    // Obtener autenticación
    public Authentication getAuthentication(String token) {
        // Obtener las claims del token
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        // Obtener el rol del token
        String role = claims.get("role").toString();

        // Crear una lista de GrantedAuthority a partir del rol
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // El principal será el email del usuario que viene en el subject del JWT
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // Validar tokens JWT
    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}