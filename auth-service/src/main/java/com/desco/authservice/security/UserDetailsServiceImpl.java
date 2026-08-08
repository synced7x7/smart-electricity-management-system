package com.desco.authservice.security;

import com.desco.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads a {@link com.desco.auth.entity.User} by email for Spring Security.
 * The username field is the user's email address.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userRepository.findByEmail(email)
                .filter(u -> u.isActive())
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                ))
                .orElseThrow(() -> {
                    log.warn("User not found or inactive for email: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });
    }
}
