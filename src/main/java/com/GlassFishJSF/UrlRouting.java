package com.GlassFishJSF;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebFilter(filterName = "UrlRouting", urlPatterns = {"/*"})
public class UrlRouting implements Filter {

    private static final Map<String, String> URL_MAPPINGS = Map.of(
            "/", "/index.xhtml",
            "/drive", "/drive.xhtml",
            "/admin", "/admin.xhtml",
            "/login", "/login.xhtml"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // DEBUG
        System.out.println("🔍 UrlRewriteFilter - URI: " + requestURI);
        System.out.println("🔍 UrlRewriteFilter - Path: " + path);

        // Ignorer les ressources JSF et statiques
        if (path.startsWith("/resources/") ||
                path.startsWith("/javax.faces.resource/") ||
                path.startsWith("/jakarta.faces.resource/") || // ✅ ajout
                path.matches(".*\\.(css|js|png|jpg|gif|ico|woff|ttf)$")) {
            chain.doFilter(request, response);
            return;
        }

        // Ignorer les requêtes AJAX JSF
        if (httpRequest.getParameter("javax.faces.partial.ajax") != null) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Laisser passer les .xhtml directement (pas de redirection inverse vers clean)
        if (path.endsWith(".xhtml")) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Forward uniquement des URLs propres vers leur .xhtml correspondant
        String targetPath = URL_MAPPINGS.get(path);
        if (targetPath != null) {
            System.out.println("➡️ Forward: " + path + " → " + targetPath);
            request.getRequestDispatcher(targetPath).forward(request, response);
            return;
        }

        // Aucune règle → continuer normalement
        chain.doFilter(request, response);
    }
}
