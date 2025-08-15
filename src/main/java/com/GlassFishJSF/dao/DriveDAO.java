package com.GlassFishJSF.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.File;
import java.util.List;


import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

import java.io.*;
import java.nio.file.*;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

@Named
@ViewScoped
public class DriveDAO implements Serializable {

    private static final String BASE_PATH = "C:\\Users\\Romain\\Desktop\\GlassFishDrive"; // 📂
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10 Go en bytes


    private String currentPath = BASE_PATH;
    private String newFolderName;
    private String renameOldName;
    private String renameNewName;

    // UPLOAD FILE
    private boolean uploading = false;
    private long uploadProgress = 0;
    private String uploadStatusMessage = "";

    // Getters et setters
    public boolean isUploading() { return uploading; }
    public void setUploading(boolean uploading) { this.uploading = uploading; }
    public long getUploadProgress() { return uploadProgress; }
    public void setUploadProgress(long uploadProgress) { this.uploadProgress = uploadProgress; }
    public String getUploadStatusMessage() { return uploadStatusMessage; }
    public void setUploadStatusMessage(String uploadStatusMessage) { this.uploadStatusMessage = uploadStatusMessage; }

    private UploadedFiles uploadFile; // ou renomme en uploadFiles

    public UploadedFiles getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(UploadedFiles uploadFile) {
        this.uploadFile = uploadFile;
    }





    // ------------ VERIFICATION ------------

    private void showErrorToast(String message) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message)
        );
    }

    // Vérifie que le dossier courant existe, sinon retourne à la racine (pour navigation)
    private void ensureCurrentPathValid() {
        Path path = Paths.get(currentPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            currentPath = BASE_PATH;
        }
    }

    // Vérifie que le dossier courant existe, sinon lance une exception (pour actions critiques)
    private void validateCurrentPathOrThrowAndReset() throws IOException {
        Path path = Paths.get(currentPath);

        // Sécurité : on vérifie que le chemin est bien sous BASE_PATH
        if (!path.normalize().startsWith(Paths.get(BASE_PATH))) {
            currentPath = BASE_PATH;
            showErrorToast("Chemin non autorisé.");
            throw new IOException("Chemin non autorisé : " + currentPath);
        }

        // Vérifie chaque sous-dossier depuis la racine
        Path testPath = Paths.get(BASE_PATH);
        for (Path part : Paths.get(BASE_PATH).relativize(path)) {
            testPath = testPath.resolve(part);
            if (!Files.exists(testPath) || !Files.isDirectory(testPath)) {
                currentPath = BASE_PATH; // 🔹 Reset à la racine
                showErrorToast("Le dossier a été renommé ou supprimé. Vous êtes revenu à la racine.");
                throw new IOException("[VERIFICATION] Le dossier n'existe plus ou a été renommé : " + testPath);
            }
        }
    }

    // Gère les conflits de noms : si un fichier/dossier existe déjà, on ajoute (1), (2), ...
    private String resolveConflictName(String name) {
        Path path = Paths.get(currentPath, name);
        if (!Files.exists(path)) {
            return name; // aucun conflit, on garde le nom
        }

        String baseName = name;
        String extension = "";

        // Sépare nom et extension si c'est un fichier avec extension
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }

        int counter = 1;
        String newName;
        do {
            newName = baseName + "(" + counter + ")" + extension;
            counter++;
        } while (Files.exists(Paths.get(currentPath, newName)));

        return newName;
    }

    // ------------ CRUD ------------

    // --- GET

    public List<String> getFiles() {
        File dir = new File(currentPath);
        if (!dir.exists() || !dir.isDirectory()) return Collections.emptyList();

        String[] files = dir.list((current, name) -> new File(current, name).isFile());
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public List<String> getFolders() {
        File dir = new File(currentPath);
        if (!dir.exists() || !dir.isDirectory()) return Collections.emptyList();

        String[] folders = dir.list((current, name) -> new File(current, name).isDirectory());
        return folders != null ? Arrays.asList(folders) : Collections.emptyList();
    }

    // --- ADD


    public void handleUploadFile(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try (InputStream in = file.getInputStream()) {
            Path target = Paths.get(currentPath, resolveConflictName(file.getFileName()));
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Upload réussi", "Fichier : " + file.getFileName()));
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur upload", e.getMessage()));
        }
    }


    private void uploadWithProgress(InputStream inputStream, Path target, long totalSize) throws IOException {
        final int BUFFER_SIZE = 8192; // 8KB buffer
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;

        uploadStatusMessage = "Upload en cours...";

        try (InputStream in = inputStream;
             OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 🔹 Mise à jour de la progression
                if (totalSize > 0) {
                    uploadProgress = (totalBytesRead * 100) / totalSize;
                    uploadStatusMessage = String.format("Upload en cours... %s / %s (%.1f%%)",
                            formatFileSize(totalBytesRead),
                            formatFileSize(totalSize),
                            (double)uploadProgress);
                }

                // 🔹 Permet à JSF de mettre à jour l'interface (optionnel)
                try {
                    Thread.sleep(10); // Petite pause pour éviter la surcharge
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Upload interrompu", e);
                }
            }
        }
    }

    /**
     * Formate la taille de fichier de manière lisible
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "o";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Validation client-side de la taille (appelée via JavaScript)
     */
    public void validateFileSize() {
        // Cette méthode peut être appelée via AJAX pour une validation préalable
        if (uploadFile != null && uploadFile.getSize() > MAX_FILE_SIZE) {
            String maxSizeFormatted = formatFileSize(MAX_FILE_SIZE);
            String fileSizeFormatted = formatFileSize(uploadFile.getSize());
            addErrorMessage("Fichier trop volumineux : " + fileSizeFormatted +
                    " (max: " + maxSizeFormatted + ")");
        }
    }

    public void cancelUpload() {
        uploading = false;
        uploadProgress = 0;
        uploadStatusMessage = "Upload annulé";
        addInfoMessage("Upload annulé par l'utilisateur");
    }

    private void resetUploadState() {
        uploadFile = null;
        uploadProgress = 0;
        uploadStatusMessage = "";
        // Le uploading reste à false
    }
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }

    /**
     * Ajoute un message d'information
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", message));
    }




    public void handleCreateFolder() {
        System.out.println("--[FOLDER] " + currentPath + "\\" + newFolderName);
        try {
            validateCurrentPathOrThrowAndReset(); // 🔹 Bloque si le dossier a disparu
            Files.createDirectories(Paths.get(currentPath, newFolderName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // --- DELETE
    public void delete(String filename) {
        try {
            validateCurrentPathOrThrowAndReset(); // 🔹 Bloque si le dossier a disparu
            Path target = Paths.get(currentPath, filename);
            if (Files.exists(target)) {
                Files.deleteIfExists(target);
            } else {
                throw new IOException("Le fichier à supprimer n'existe pas : " + filename);
            }
        } catch (IOException e) {
            System.err.println("Erreur suppression : " + e.getMessage());
        }
    }

    // --- CHANGE


    public void rename() {
        try {
            validateCurrentPathOrThrowAndReset(); // 🔹 Bloque si le dossier a disparu
            Path source = Paths.get(currentPath, renameOldName);
            if (!Files.exists(source)) {
                throw new IOException("Le fichier/dossier à renommer n'existe pas : " + renameOldName);
            }
            Path target = Paths.get(currentPath, resolveConflictName(renameNewName));
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erreur renommage : " + e.getMessage());
        }
    }

    // UTILS

    public List<String> getBreadcrumb() {
        String relativePath = currentPath.replace(BASE_PATH, "");
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }
        List<String> parts = new ArrayList<>();
        if (!relativePath.isBlank()) {
            parts.addAll(Arrays.asList(relativePath.split(Pattern.quote(File.separator))));
        }
        return parts;
    }

    public void openFolder(String folderName) {
        System.out.println("--[OPEN FOLDER] " + currentPath + "\\" + folderName);
        Path newPath = Paths.get(currentPath, folderName);

        // Sécurité : on vérifie que le chemin est bien sous BASE_PATH
        if (!newPath.normalize().startsWith(Paths.get(BASE_PATH))) {
            currentPath = BASE_PATH;
            showErrorToast("Chemin non autorisé.");
            return;
        }

        // Vérifie chaque sous-dossier depuis la racine
        Path testPath = Paths.get(BASE_PATH);
        for (Path part : Paths.get(BASE_PATH).relativize(newPath)) {
            testPath = testPath.resolve(part);
            if (!Files.exists(testPath) || !Files.isDirectory(testPath)) {
                currentPath = BASE_PATH; // 🔹 Reset à la racine
                showErrorToast("Le dossier a été renommé ou supprimé. Vous êtes revenu à la racine.");
                return;
            }
        }
        currentPath = newPath.toString();
        System.out.println("--[OPEN FOLDER] SUCESS : " + currentPath);
        System.out.println("--[OPEN FOLDER 2] " + Files.isDirectory(newPath));
        System.out.println("--[OPEN FOLDER 2] " + Files.isDirectory(newPath));


    }

    public void navigateToPath(String relativePath) {
        try {
            validateCurrentPathOrThrowAndReset(); // 🔹 Bloque si le dossier a disparu
            currentPath = Paths.get(BASE_PATH, relativePath).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String buildPathUpTo(int index) {
        List<String> parts = getBreadcrumb();
        return String.join(File.separator, parts.subList(0, index + 1));
    }




    public String getNewFolderName() {
        return newFolderName;
    }

    public void setNewFolderName(String newFolderName) {
        this.newFolderName = newFolderName;
    }

    public String getRenameOldName() {
        return renameOldName;
    }

    public void setRenameOldName(String renameOldName) {
        this.renameOldName = renameOldName;
    }

    public String getRenameNewName() {
        return renameNewName;
    }

    public void setRenameNewName(String renameNewName) {
        this.renameNewName = renameNewName;
    }
}

