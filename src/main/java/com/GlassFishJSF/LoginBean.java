package com.GlassFishJSF;

import com.GlassFishJSF.service.IPBlockingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger("SECURITY");

    @Resource(name="passwordHub")
    private String passwordHub;

    @Inject
    private IPBlockingService ipBlockingService;

    @PostConstruct
    private void init() {
        try {
            javax.naming.Context ctx = new javax.naming.InitialContext();
            String pwd = (String) ctx.lookup("passwordHub");
            System.out.println("JNDI lookup passwordHub: " + pwd);
            passwordHub = pwd;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String password;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String returnAction() {
        String clientIP = getClientIP();
        String userAgent = getUserAgent();
        String sessionId = getSessionId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (ipBlockingService.isBlocked(clientIP)) {
            LOGGER.warning(String.format("[CONNEXION] BLOCKED_IP_ATTEMPT | Password: %s | IP: %s | Timestamp: %s | SessionId: %s | UserAgent: %s",
                    sanitize(password), clientIP, timestamp, sessionId, sanitize(userAgent)));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Votre adresse IP est temporairement bloquée. Réessayez plus tard.", null));
            return null;
        }
        System.out.println(passwordHub);


        if (passwordHub.equals(password)) {
            ipBlockingService.resetFailedAttempts(clientIP);

            LOGGER.info(String.format("[CONNEXION] SUCCESS | Password: %s | IP: %s | Timestamp: %s | SessionId: %s | UserAgent: %s"
                    , sanitize(password), clientIP, timestamp, sessionId, sanitize(userAgent)));

            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(true);

            session.setAttribute("user", clientIP);

            // timeout à 30 jours (en secondes)
            session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 2592000 secondes

            return "drive?faces-redirect=true";
        } else {
            ipBlockingService.recordFailedAttempt(clientIP);

            int remainingAttempts = ipBlockingService.getRemainingAttempts(clientIP);

            if (remainingAttempts <= 0) {
                LOGGER.severe(String.format("[CONNEXION] IP_BLOCKED | Password: %s | IP: %s | Timestamp: %s | SessionId: %s | UserAgent: %s | TotalAttempts: %d",
                        sanitize(password), clientIP, timestamp, sessionId, sanitize(userAgent), 3));
            } else {
                LOGGER.warning(String.format("[CONNEXION] FAILED | Password: %s | IP: %s | Timestamp: %s | SessionId: %s | UserAgent: %s | RemainingAttempts: %d",
                        sanitize(password), clientIP, timestamp, sessionId, sanitize(userAgent), remainingAttempts));
            }

            String errorMessage;
            if (remainingAttempts > 0) {
                errorMessage = "Code d'accès incorrect.";
            } else {
                errorMessage = "Trop de tentatives échouées. Votre IP est temporairement bloquée.";
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, null));
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format("%d tentatives restantes.",
                            remainingAttempts), null));
            return null;
        }
    }

    public String logout() {
        String clientIP = getClientIP();
        String sessionId = getSessionId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        LOGGER.info(String.format("[CONNEXION] LOGOUT | IP: %s | Timestamp: %s | SessionId: %s",
                clientIP, timestamp, sessionId));

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login?faces-redirect=true";
    }

    private String getClientIP() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();

        // Vérifier les en-têtes proxy courants
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "UNKNOWN";
    }

    private String getSessionId() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        if (request.getSession(false) != null) {
            return request.getSession(false).getId();
        }
        return "NO_SESSION";
    }

    private String sanitize(String input) {
        if (input == null) return "NULL";
        return input.replaceAll("[\r\n\t|]", "_").trim();
    }
}