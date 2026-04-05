(function() {
    let isRefreshing = false;

    const formatTime = (ms) => {
        const s = Math.floor(ms / 1000);
        const m = Math.floor(s / 60);
        const h = Math.floor(m / 60);
        const d = Math.floor(h / 24);

        if (d > 0) return `${d}j ${h % 24}h`;
        if (h > 0) return `${h}h ${m % 60}m`;
        if (m > 0) return `${m}m ${s % 60}s`;
        return `${s}s`;
    };

    const updateProgress = () => {
        const now = Date.now();
        const containers = document.querySelectorAll('.js-progress-container');

        containers.forEach((container) => {
            const start = Number(container.getAttribute('data-start').replace(/\s/g, ''));
            const end = Number(container.getAttribute('data-end').replace(/\s/g, ''));

            const totalMs = end - start;
            const elapsedMs = now - start;
            const remainingMs = end - now;

            if (remainingMs <= 0 && !isRefreshing) {
                isRefreshing = true;
                if (typeof triggerRefresh === "function") {
                    triggerRefresh();
                    setTimeout(() => { isRefreshing = false; }, 2000);
                }
                return;
            }

            const percent = Math.min(100, Math.max(0, (elapsedMs / totalMs) * 100));
            const percentDisplay = percent.toFixed(2);

            const fill = container.querySelector('.js-progress-fill');
            const timerLabel = container.querySelector('.js-timer-label');

            if (fill) fill.style.width = percentDisplay + '%';
            if (timerLabel) {
                if (remainingMs <= 0) {
                    timerLabel.textContent = "(100%) Terminé";
                } else {
                    const formattedTime = formatTime(remainingMs);
                    timerLabel.textContent = `(${percentDisplay}%) ${formattedTime} restant`;
                }
            }
        });
    };

    setInterval(updateProgress, 1000);
    updateProgress();
})();