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
        "/drive.xhtml", "/drive",
        "/login.xhtml", "/login"
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
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String path = req.getRequestURI().substring(contextPath.length());

        HttpSession session = req.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute("user") != null;

        // 🔒 Si déjà connecté → accès à /login interdit
        if (path.equals("/login") || path.equals("/login.xhtml")) {
            if (isLoggedIn) {
                resp.sendRedirect(contextPath + "/drive");
                return;
            }
        }

        // 🔒 Si page protégée et pas loggé → rediriger vers login
        if (PROTECTED_PATHS.contains(path)) {
            if (!isLoggedIn) {
                resp.sendRedirect(contextPath + "/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }

}