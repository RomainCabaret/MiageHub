// Variables globales pour le suivi de l'upload
let uploadStartTime = 0;
let uploadedBytes = 0;
let totalBytes = 0;
let uploadInterval = null;

// Fonction pour ouvrir le modal d'upload
function openUploadModal() {
    console.log('🚀 Ouverture du modal d\'upload');
    const modal = document.getElementById('modal-upload-progress');
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');

    // Réinitialiser les valeurs
    resetUploadProgress();

    // Démarrer le suivi de la progression
    startUploadTracking();

    // Empêcher la fermeture accidentelle
    document.body.style.overflow = 'hidden';
}

// Fonction pour fermer le modal d'upload
function closeUploadModal() {
    console.log('✅ Fermeture du modal d\'upload');
    const modal = document.getElementById('modal-upload-progress');
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');

    // Nettoyer
    stopUploadTracking();
    document.body.style.overflow = '';

    // Afficher le succès
    updateUploadStatus('✅ Upload terminé avec succès !', 'success');
    setTimeout(() => {
        resetUploadProgress();
    }, 2000);
}

// Fonction pour réinitialiser la progression
function resetUploadProgress() {
    document.getElementById('upload-percentage').textContent = '0%';
    document.getElementById('upload-progress-fill').style.width = '0%';
    document.getElementById('upload-file-name').textContent = 'Préparation du fichier...';
    document.getElementById('upload-speed').textContent = 'Vitesse: -- KB/s';
    document.getElementById('upload-eta').textContent = 'Temps restant: --';
    updateUploadStatus('🔄 Initialisation...', 'loading');
}

// Fonction pour démarrer le suivi de l'upload
function startUploadTracking() {
    uploadStartTime = Date.now();
    uploadedBytes = 0;

    // Simuler la progression (à adapter selon votre implémentation réelle)
    let progress = 0;
    uploadInterval = setInterval(() => {
        if (progress < 90) {
            progress += Math.random() * 10;
            updateUploadProgress(Math.min(progress, 90));
        }
    }, 500);

    updateUploadStatus('📤 Téléversement en cours...', 'uploading');
}

// Fonction pour arrêter le suivi
function stopUploadTracking() {
    if (uploadInterval) {
        clearInterval(uploadInterval);
        uploadInterval = null;
    }
}

// Fonction pour mettre à jour la progression
function updateUploadProgress(percentage) {
    const progressFill = document.getElementById('upload-progress-fill');
    const progressText = document.getElementById('upload-percentage');

    progressFill.style.width = percentage + '%';
    progressText.textContent = Math.round(percentage) + '%';

    // Calculer la vitesse et le temps restant (estimation)
    const currentTime = Date.now();
    const elapsedTime = (currentTime - uploadStartTime) / 1000;

    if (elapsedTime > 0 && percentage > 0) {
        const speed = (percentage / elapsedTime) * 1024; // KB/s simulé
        const remainingTime = ((100 - percentage) / percentage) * elapsedTime;

        document.getElementById('upload-speed').textContent =
            `Vitesse: ${Math.round(speed)} KB/s`;
        document.getElementById('upload-eta').textContent =
            `Temps restant: ${formatTime(remainingTime)}`;
    }
}

// Fonction pour mettre à jour le statut
function updateUploadStatus(message, type = 'info') {
    const statusMessage = document.getElementById('upload-status-message');
    statusMessage.innerHTML = message;
    statusMessage.className = `status-message status-message--${type}`;
}

// Fonction pour annuler l'upload
function cancelUpload() {
    if (confirm('Êtes-vous sûr de vouloir annuler le téléversement ?')) {
        stopUploadTracking();
        updateUploadStatus('❌ Upload annulé', 'error');

        // Ici vous devriez implémenter la logique pour annuler l'upload côté serveur
        // Par exemple, appeler une méthode AJAX ou JSF

        setTimeout(() => {
            closeUploadModal();
        }, 1000);
    }
}

// Fonction utilitaire pour formater le temps
function formatTime(seconds) {
    if (seconds < 60) {
        return Math.round(seconds) + 's';
    } else if (seconds < 3600) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = Math.round(seconds % 60);
        return `${minutes}m ${remainingSeconds}s`;
    } else {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours}h ${minutes}m`;
    }
}

// Fonction pour formater la taille de fichier
function formatFileSize(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'Ko', 'Mo', 'Go', 'To'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

// Gestion de la fermeture de la page pendant l'upload
window.addEventListener('beforeunload', function (e) {
    const modal = document.getElementById('modal-upload-progress');
    if (modal && modal.style.display === 'flex') {
        e.preventDefault();
        e.returnValue = 'Un téléversement est en cours. Êtes-vous sûr de vouloir quitter ?';
        return e.returnValue;
    }
});

// Validation côté client + lancement de l'upload
function validateAndUpload(input) {
    const file = input.files[0];
    if (!file) return;

    const maxSize = 10 * 1024 * 1024 * 1024; // 10 Go

    if (file.size > maxSize) {
        const maxSizeFormatted = formatFileSize(maxSize);
        const fileSizeFormatted = formatFileSize(file.size);

        alert(`❌ Fichier trop volumineux!\n\n` +
            `Taille du fichier: ${fileSizeFormatted}\n` +
            `Taille maximum autorisée: ${maxSizeFormatted}`);

        input.value = '';
        return;
    }

    const forbiddenExtensions = ['.exe', '.bat', '.com', '.scr', '.pif'];
    const fileName = file.name.toLowerCase();
    const hasForbiddenExtension = forbiddenExtensions.some(ext => fileName.endsWith(ext));

    if (hasForbiddenExtension) {
        alert(`❌ Type de fichier non autorisé!\n\nLes fichiers exécutables ne sont pas acceptés pour des raisons de sécurité.`);
        input.value = '';
        return;
    }

    if (file.size > 1024 * 1024 * 1024) {
        const fileSizeFormatted = formatFileSize(file.size);
        if (!confirm(`⚠️ Fichier volumineux détecté!\n\n` +
            `Taille: ${fileSizeFormatted}\n\n` +
            `L'upload peut prendre du temps. Continuer?`)) {
            input.value = '';
            return;
        }
    }

    // Mettre à jour le nom du fichier dans le modal
    document.getElementById('upload-file-name').textContent = `Fichier: ${file.name}`;
    totalBytes = file.size;

    console.log('🔄 Lancement de l\'upload:', file.name, formatFileSize(file.size));
    // Le modal s'ouvrira automatiquement grâce à onstart="openUploadModal()"
}
