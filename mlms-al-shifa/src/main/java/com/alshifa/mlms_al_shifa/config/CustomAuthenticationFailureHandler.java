package com.alshifa.mlms_al_shifa.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class CustomAuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {

        String redirectUrl = "/login?error=true";

        if (exception instanceof DisabledException) {
            redirectUrl = "/login?disabled=true";
        } else if (exception instanceof LockedException) {
            redirectUrl = "/login?locked=true";
        } else if (exception instanceof CredentialsExpiredException) {
            redirectUrl = "/login?credentialsExpired=true";
        } else if (exception instanceof BadCredentialsException) {
            redirectUrl = "/login?error=true";
        }

        response.sendRedirect(redirectUrl);
    }
}