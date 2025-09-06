package com.GlassFishJSF.dao;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.faces.context.ExternalContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.annotation.Resource;

@Named
@ViewScoped
public class DriveDAO implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(DriveDAO.class.getName());

    // Configuration
    @Resource(name="BASE_PATH")
    private String BASE_PATH;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 Mo


    @PostConstruct
    private void init() {
        try {
            javax.naming.Context ctx = new javax.naming.InitialContext();
            String absolutePath = (String) ctx.lookup("BASE_PATH");
            System.out.println("JNDI lookup BASE_PATH: " + absolutePath);
            BASE_PATH = absolutePath;
            currentPath = absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Variables d'état
    private String currentPath = BASE_PATH;
    private String newFolderName;
    private String renameOldName;
    private String renameNewName;

    // IMPORTANT : Variable pour le mode simple selon la doc PrimeFaces
    private UploadedFile uploadedFile;

    // ==================== GETTERS/SETTERS ====================

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getNewFolderName() { return newFolderName; }
    public void setNewFolderName(String newFolderName) { this.newFolderName = newFolderName; }

    public String getRenameOldName() { return renameOldName; }
    public void setRenameOldName(String renameOldName) { this.renameOldName = renameOldName; }

    public String getRenameNewName() { return renameNewName; }
    public void setRenameNewName(String renameNewName) { this.renameNewName = renameNewName; }

    private void resetUploadedFile() {
        uploadedFile = null;
    }

    // ==================== UPLOAD SELON DOC PRIMEFACES ====================

    /**
     * Listener pour le mode simple avec auto="true"
     * Signature exacte selon la documentation PrimeFaces
     */
    public void handleFileUpload(FileUploadEvent event) {
        System.out.println("=== DÉBUT handleFileUpload (Event) ===");

        try {
            UploadedFile file = event.getFile();
            System.out.println("Fichier reçu via event: " + (file != null ? file.getFileName() : "NULL"));

            if (file != null) {
                processFile(file);
            } else {
                addErrorMessage("Aucun fichier reçu dans l'événement");
            }

        } catch (Exception e) {
            System.err.println("ERREUR handleFileUpload (Event): " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur upload via event: " + e.getMessage());
        }

        System.out.println("=== FIN handleFileUpload (Event) ===");
    }

    /**
     * Logique commune de traitement du fichier
     */
    private void processFile(UploadedFile file) {
        try {
            validateCurrentPath();
            validateFile(file);

            String finalName = resolveNameConflict(file.getFileName());
            Path targetPath = Paths.get(currentPath).resolve(finalName).normalize();

            Files.createDirectories(targetPath.getParent());

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            if (!Files.exists(targetPath)) {
                throw new IOException("Échec de création du fichier");
            }

            addSuccessMessage("Fichier uploadé avec succès : " + finalName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur upload fichier", e);
            addErrorMessage("Erreur upload : " + e.getMessage());
        } finally {
            resetUploadedFile();
        }
    }

    // ==================== VALIDATIONS ====================

    private void validateCurrentPath() throws IOException {
        if (BASE_PATH == null || BASE_PATH.isEmpty()) {
            throw new IOException("BASE_PATH non configuré");
        }

        Path basePath = Paths.get(BASE_PATH).toAbsolutePath().normalize();
        if (currentPath == null || currentPath.isEmpty()) {
            currentPath = basePath.toString();
        }

        Path normalizedCurrent = Paths.get(currentPath).toAbsolutePath().normalize();
        if (!normalizedCurrent.startsWith(basePath)) {
            currentPath = basePath.toString();
            throw new SecurityException("Chemin non autorisé");
        }

        if (!Files.exists(normalizedCurrent)) {
            Files.createDirectories(normalizedCurrent);
        }

        currentPath = normalizedCurrent.toString();
    }

    private void validateFile(UploadedFile file) throws IOException {
        if (file == null || file.getFileName() == null || file.getFileName().trim().isEmpty()) {
            throw new IOException("Aucun fichier sélectionné");
        }

        if (file.getSize() <= 0) {
            throw new IOException("Fichier vide");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException(String.format("Fichier trop volumineux (%.1f Mo max)", MAX_FILE_SIZE / (1024.0 * 1024.0)));
        }

        // Extensions autorisées (Linux-case insensitive)
        String lowerName = file.getFileName().toLowerCase(Locale.ROOT);
        List<String> allowed = List.of(".jpg",".jpeg",".png",".gif",".pdf",".txt",".zip",".rar",".docx",".doc",".xlsx");
        boolean valid = allowed.stream().anyMatch(lowerName::endsWith);
        if (!valid) throw new IOException("Extension non autorisée : " + lowerName);
    }


    private String resolveNameConflict(String fileName) {
        Path targetPath = Paths.get(currentPath, fileName);
        if (!Files.exists(targetPath)) {
            return fileName;
        }

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        String newName;
        do {
            newName = baseName + "_(" + counter + ")" + extension;
            targetPath = Paths.get(currentPath, newName);
            counter++;
        } while (Files.exists(targetPath) && counter < 100);

        return newName;
    }

    // ==================== CRUD OPERATIONS ====================

    public List<String> getFiles() {
        try {
            validateCurrentPath();
            File dir = new File(currentPath);
            String[] files = dir.list((current, name) -> new File(current, name).isFile());
            List<String> result = files != null ? Arrays.asList(files) : Collections.emptyList();
            Collections.sort(result);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lecture fichiers", e);
            return Collections.emptyList();
        }
    }

    public List<String> getFolders() {
        try {
            validateCurrentPath();
            File dir = new File(currentPath);
            String[] folders = dir.list((current, name) -> new File(current, name).isDirectory());
            List<String> result = folders != null ? Arrays.asList(folders) : Collections.emptyList();
            Collections.sort(result);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lecture dossiers", e);
            return Collections.emptyList();
        }
    }

    public void handleCreateFolder() {
        try {
            validateCurrentPath();

            if (newFolderName == null || newFolderName.trim().isEmpty()) {
                addErrorMessage("Nom de dossier requis");
                return;
            }

            String finalFolderName = resolveNameConflict(newFolderName);
            Path folderPath = Paths.get(currentPath, finalFolderName);

            Files.createDirectories(folderPath);
            addSuccessMessage("Dossier '" + finalFolderName + "' créé");

            newFolderName = "";

        } catch (Exception e) {
            addErrorMessage("Erreur création dossier: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Erreur création dossier", e);
        }
    }

    public void delete(String itemName) {
        try {
            validateCurrentPath();
            Path targetPath = Paths.get(currentPath, itemName);

            if (!Files.exists(targetPath)) {
                addErrorMessage("L'élément n'existe pas");
                return;
            }

            Files.delete(targetPath);
            addSuccessMessage("Élément supprimé: " + itemName);

        } catch (Exception e) {
            addErrorMessage("Erreur suppression: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Erreur suppression", e);
        }
    }

    public void rename() {
        try {
            validateCurrentPath();

            if (renameOldName == null || renameNewName == null ||
                    renameOldName.trim().isEmpty() || renameNewName.trim().isEmpty()) {
                addErrorMessage("Noms requis pour le renommage");
                return;
            }

            Path sourcePath = Paths.get(currentPath, renameOldName);
            if (!Files.exists(sourcePath)) {
                addErrorMessage("L'élément à renommer n'existe pas");
                return;
            }

            String finalNewName = resolveNameConflict(renameNewName);
            Path targetPath = Paths.get(currentPath, finalNewName);

            Files.move(sourcePath, targetPath);
            addSuccessMessage("Renommé: " + renameOldName + " → " + finalNewName);

            renameOldName = "";
            renameNewName = "";

        } catch (Exception e) {
            addErrorMessage("Erreur renommage: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Erreur renommage", e);
        }
    }

    // ==================== NAVIGATION ====================

    public void openFolder(String folderName) {
        try {
            Path newPath = Paths.get(currentPath, folderName).normalize();

            if (!newPath.startsWith(Paths.get(BASE_PATH))) {
                addErrorMessage("Accès non autorisé");
                return;
            }

            if (!Files.exists(newPath) || !Files.isDirectory(newPath)) {
                addErrorMessage("Le dossier n'existe pas");
                return;
            }

            currentPath = newPath.toString();

        } catch (Exception e) {
            addErrorMessage("Erreur navigation: " + e.getMessage());
        }
    }

    public void navigateToPath(String relativePath) {
        try {
            Path newPath = relativePath.isEmpty() ?
                    Paths.get(BASE_PATH) :
                    Paths.get(BASE_PATH, relativePath);

            newPath = newPath.normalize();

            if (!newPath.startsWith(Paths.get(BASE_PATH))) {
                addErrorMessage("Chemin non autorisé");
                return;
            }

            if (Files.exists(newPath) && Files.isDirectory(newPath)) {
                currentPath = newPath.toString();
            } else {
                addErrorMessage("Le chemin n'existe pas");
            }

        } catch (Exception e) {
            addErrorMessage("Erreur navigation: " + e.getMessage());
        }
    }

    public List<String> getBreadcrumb() {
        try {
            String relativePath = Paths.get(BASE_PATH).relativize(Paths.get(currentPath)).toString();
            if (relativePath.equals(".")) {
                return Collections.emptyList();
            }
            return Arrays.asList(relativePath.split("\\\\"));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String buildPathUpTo(int index) {
        try {
            List<String> parts = getBreadcrumb();
            if (index >= parts.size()) return String.join("\\", parts);
            return String.join("\\", parts.subList(0, index + 1));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Télécharge un fichier
     * @param fileName Le nom du fichier à télécharger
     */
    public void downloadFile(String fileName) {
        System.out.println("=== DÉBUT downloadFile ===");
        System.out.println("Fichier demandé: " + fileName);

        try {
            validateCurrentPath();

            if (fileName == null || fileName.trim().isEmpty()) {
                addErrorMessage("Nom de fichier requis pour le téléchargement");
                return;
            }

            Path filePath = Paths.get(currentPath, fileName);
            System.out.println("Chemin complet: " + filePath);

            // Vérifications de sécurité
            if (!filePath.normalize().startsWith(Paths.get(BASE_PATH).normalize())) {
                addErrorMessage("Accès non autorisé au fichier");
                return;
            }

            if (!Files.exists(filePath)) {
                addErrorMessage("Le fichier n'existe pas: " + fileName);
                return;
            }

            if (!Files.isRegularFile(filePath)) {
                addErrorMessage("L'élément n'est pas un fichier");
                return;
            }

            // Obtenir le contexte JSF
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

            // Déterminer le type MIME
            String mimeType = determineMimeType(fileName);
            System.out.println("Type MIME détecté: " + mimeType);

            // Configurer les headers de la réponse
            response.setContentType(mimeType);
            response.setContentLengthLong(Files.size(filePath));

            // Encoder le nom de fichier pour éviter les problèmes avec les caractères spéciaux
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);

            // Éviter la mise en cache
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            // Copier le fichier vers la réponse
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                response.getOutputStream().flush();
                System.out.println("Téléchargement terminé: " + totalBytes + " bytes envoyés");
            }

            // Marquer la réponse comme complète pour JSF
            facesContext.responseComplete();

            System.out.println("=== TÉLÉCHARGEMENT RÉUSSI ===");

        } catch (Exception e) {
            System.err.println("ERREUR downloadFile: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur lors du téléchargement: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Erreur téléchargement fichier: " + fileName, e);
        }
    }



    /**
     * Méthode utilitaire pour formater la taille des fichiers
     * @param fileName Le nom du fichier
     * @return La taille formatée du fichier
     */
    public String getFileSize(String fileName) {
        try {
            validateCurrentPath();
            Path filePath = Paths.get(currentPath, fileName);

            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                long size = Files.size(filePath);
                return formatFileSize(size);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lecture taille fichier: " + fileName, e);
        }
        return "N/A";
    }



    // ==================== UTILITAIRES ====================

    /**
     * Détermine le type MIME basé sur l'extension du fichier
     * @param fileName Le nom du fichier
     * @return Le type MIME correspondant
     */
    private String determineMimeType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        // Images
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        }
        // Documents
        else if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerFileName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        // Archives
        else if (lowerFileName.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerFileName.endsWith(".rar")) {
            return "application/x-rar-compressed";
        }
        // Par défaut
        else {
            return "application/octet-stream";
        }
    }

    /**
     * Formate la taille d'un fichier en unités lisibles
     * @param size La taille en bytes
     * @return La taille formatée (ex: "1.5 MB")
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }

    // Méthode de test
    public void testMethod() {
        System.out.println("=== MÉTHODE DE TEST APPELÉE ===");
        addSuccessMessage("Test réussi ! La communication JSF fonctionne.");
    }
}