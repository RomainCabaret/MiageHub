package com.GlassFishJSF;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/home.xhtml"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false); // Ne crée PAS de session

        boolean isLoggedIn = session != null && session.getAttribute("user") != null;

        if (!isLoggedIn) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/login.xhtml");
        } else {
            chain.doFilter(request, response); // Autoriser l'accès
        }
    }
}
