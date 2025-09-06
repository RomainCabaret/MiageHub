package com.GlassFishJSF;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/admin.xhtml", "/admin",
        "/drive.xhtml", "/drive"
})
public class AuthFilter implements Filter {

    // Pages protégées (anciennes ET nouvelles URLs)
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/admin.xhtml", "/admin",
            "/drive.xhtml", "/drive"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Debug
        System.out.println("🔒 AuthFilter - Path: " + path);

        // Vérifier si c'est une page protégée
        if (PROTECTED_PATHS.contains(path)) {
            HttpSession session = req.getSession(false);
            boolean isLoggedIn = session != null && session.getAttribute("user") != null;

            if (!isLoggedIn) {
                System.out.println("🔒 Accès refusé pour: " + path);
                ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/login");
                return;
            } else {
                System.out.println("🔒 Accès autorisé pour: " + path);
            }
        }

        chain.doFilter(request, response);
    }
}