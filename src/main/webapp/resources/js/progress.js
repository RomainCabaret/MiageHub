(function() {
    console.log("🚀 Script progress.js chargé et démarré !");
    let isRefreshing = false; // Anti-boucle infinie

    const updateProgress = () => {
        const now = Date.now();
        const containers = document.querySelectorAll('.js-progress-container');

        if (containers.length === 0) {
            console.warn("⚠️ Aucun élément '.js-progress-container' trouvé dans le DOM.");
            return;
        }

        containers.forEach((container, index) => {
            const startAttr = container.getAttribute('data-start');
            const endAttr = container.getAttribute('data-end');
            const label = container.querySelector('.card-label')?.textContent;

            console.log(`📊 [Barre ${index}] ${label} | start: ${startAttr} | end: ${endAttr}`);


            if (!startAttr || !endAttr) {
                console.error(`❌ Données manquantes pour la barre ${index}`);
                return;
            }

            const start = Number(startAttr.replace(/\s/g, ''));
            const end = Number(endAttr.replace(/\s/g, ''));



            if (isNaN(start) || isNaN(end)) {
                console.error(`❌ Erreur de conversion Numérique : start=${start}, end=${end}`);
                return;
            }

            const totalMs = end - start;
            const elapsedMs = now - start;
            const remainingMs = end - now;

            if (remainingMs <= 0 && !isRefreshing) {
                isRefreshing = true;
                console.log("🔄 Fin du chrono, demande de rafraîchissement...");

                if (typeof triggerRefresh === "function") {
                    triggerRefresh();

                    setTimeout(() => { isRefreshing = false; }, 2000);
                }
                return;
            }



            let percent = (elapsedMs / totalMs) * 100;
            percent = Math.min(100, Math.max(0, percent));

            const fill = container.querySelector('.js-progress-fill');
            const timerLabel = container.querySelector('.js-timer-label');

            if (fill) fill.style.width = percent.toFixed(2) + '%';
            if (timerLabel) {
                if (remainingMs <= 0) {
                    timerLabel.textContent = "Terminé";
                } else {
                    const diffSec = Math.floor(remainingMs / 1000);
                    timerLabel.textContent = `${diffSec}s restantes`;
                }
            }
        });
    };

    setInterval(updateProgress, 1000);
    updateProgress();
})();