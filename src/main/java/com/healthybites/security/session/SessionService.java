package com.healthybites.security.session;

import com.healthybites.dto.AuthResponseDTO;
import com.healthybites.dto.LoginDTO;
import com.healthybites.mapper.UsuarioMapper;
import com.healthybites.model.entity.Usuario;
import com.healthybites.security.UserPrincipal;
import com.healthybites.security.ratelimit.AuthRateLimiter;
import com.healthybites.security.token.AccessTokenService;
import com.healthybites.security.token.AccessTokenService.AccessToken;
import com.healthybites.security.token.InvalidRefreshTokenException;
import com.healthybites.security.token.RefreshTokenService;
import com.healthybites.security.token.RefreshTokenService.IssuedRefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sesiones: login, renovación y logout. Cada sesión es un access token corto (en el cuerpo de la respuesta) más un
 * refresh token rotatorio (en una cookie, ver {@link RefreshTokenCookies}).
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;
    private final AuthRateLimiter rateLimiter;
    private final UsuarioMapper usuarioMapper;

    public record Session(AuthResponseDTO body, IssuedRefreshToken refreshToken) {
    }

    @Transactional
    public Session login(LoginDTO login, String clientIp) {
        rateLimiter.checkLogin(clientIp, login.getCorreo());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getCorreo(), login.getContrasena()));
        } catch (AuthenticationException e) {
            rateLimiter.loginFailed(clientIp, login.getCorreo());
            throw e;
        }
        rateLimiter.loginSucceeded(clientIp, login.getCorreo());

        Usuario usuario = ((UserPrincipal) authentication.getPrincipal()).getUsuario();
        return open(usuario, refreshTokens.issue(usuario));
    }

    /** La revocación por reutilización debe confirmarse aunque se lance la excepción. */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Session refresh(String refreshToken, String clientIp) {
        rateLimiter.checkRefresh(clientIp);
        IssuedRefreshToken next = refreshTokens.rotate(refreshToken);
        return open(next.usuario(), next);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.revokeSession(refreshToken);
    }

    private Session open(Usuario usuario, IssuedRefreshToken refreshToken) {
        AccessToken access = accessTokens.issue(usuario);
        return new Session(usuarioMapper.toAuthResponseDTO(usuario, access.value(), access.expiresInSeconds()),
                refreshToken);
    }
}
