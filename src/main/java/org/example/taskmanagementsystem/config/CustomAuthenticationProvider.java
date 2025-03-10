package org.example.taskmanagementsystem.config;

import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws BadCredentialsException {
        UserDetails user = userService.loadUserByUsername(authentication.getName());

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (user.getUsername().equals(username) && passwordEncoder.matches(password , user.getPassword())){
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        }else{
            throw new BadCredentialsException("Неправильная почта или пароль!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}