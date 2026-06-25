package com.evently.reserva.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String email = null;
            String role = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                email = jwtUtil.extractEmail(token);
                role = jwtUtil.extractRole(token);

                // Log preventivo para ver en consola qué datos viajan
                System.out.println("====== RESERVA JWT FILTER ======");
                System.out.println("Email extraido: " + email);
                System.out.println("Rol extraido: " + role);
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token)) {
                    // 1. Creamos la autoridad exacta asegurándonos de que no sea null
                    String rolLimpio = (role != null) ? role : "CLIENTE";
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(rolLimpio));

                    // 2. Usamos el constructor estricto de Spring para usuarios
                    // autenticados:(principal, credentials, authorities)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Autenticación inyectada con éxito para reservas con autoridad: " + rolLimpio);
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando el JWT en Reserva-Service: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
