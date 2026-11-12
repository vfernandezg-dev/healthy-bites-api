package com.healthybites.identity.app;

import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cuentas para el {@code AuthenticationManager} de Spring Security (solo se usa en el login). Una cuenta
 * desactivada falla como si la contraseña fuera incorrecta: el cliente recibe el mismo 401.
 */
@Service
class AccountUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    AccountUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isActive())
                .build();
    }
}
