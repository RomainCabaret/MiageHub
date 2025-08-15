const btnNewFolder = document.getElementById("btn-new-folder");
const btnNewFolderTb = document.getElementById(
    "btn-new-folder-toolbar"
);
const btnNewFolderEmpty = document.getElementById(
    "btn-new-folder-empty"
);

// RENAME
const modalRename = document.getElementById("modal-rename");
const modalRenameInputContent = document.getElementById("rename-input");
const modalOldInputContent = document.getElementById("old-name-input");



function openRenameModal(currentName) {
    modalRename.classList.add("is-open");
    modalRename.removeAttribute("aria-hidden");
    document.body.style.overflow = "hidden";
    modalRenameInputContent.value = currentName;
    modalOldInputContent.value = currentName;
}


// Modals FOLDER
const modalFolderElement = document.getElementById("modal-folder");

// Modals DELETE


// OPEN FOLDER DOUBLE CLICK

document.querySelectorAll(".folder-card").forEach(card => {
    card.addEventListener("dblclick", (e) => {
        const openBtn = card.querySelector(".openFolderBtn");
        if (openBtn && !e.target.classList.contains("btnRenameAction")) {
            openBtn.click();
        }
    });
});



const renameInput = document.getElementById("rename-input");
const renameConfirm = document.getElementById("rename-confirm");

const modalPreview = document.getElementById("modal-preview");
const previewContainer = document.getElementById("preview-container");

const toastRoot = document.getElementById("toast");

// Modal state
/** @type {DriveItem|null} */
let renameTarget = null; // when null => create flow
/** @type {DriveItem|null} */
let previewTarget = null;
/** @type {string|null} */
let previewUrl = null;


function isTextLike(name, type) {
    return (
        type.startsWith("text/") ||
        /\.(md|json|js|ts|tsx|css|scss|html|txt)$/i.test(name)
    );
}

function showToast(message) {
    const el = document.createElement("div");
    el.className = "toast__item";
    el.textContent = message;
    toastRoot.appendChild(el);
    setTimeout(() => {
        el.style.opacity = "0";
        el.style.transform = "translateY(-4px)";
        setTimeout(() => el.remove(), 180);
    }, 2200);
}


function newFolder() {

    console.log("LOGGER ! new folder")

    modalFolderElement.classList.add("is-open");
    modalFolderElement.removeAttribute("aria-hidden");
    document.body.style.overflow = "hidden";
    if (typeof onOpen === "function") onOpen();
}


function openModalRename() {
    const titleEl = document.getElementById("modal-rename-title");
    titleEl.textContent = title;
    renameInput.value = renameTarget?.name || "";
    openModal(modalRename, () => renameInput.focus());
}

function commitRename() {
    const value = renameInput.value.trim();
    if (!value) {
        closeModal(modalRename);
        renameTarget = null;
        return;
    }
    const existsIdx = items.findIndex(
        (i) => renameTarget && i.id === renameTarget.id
    );
    if (existsIdx >= 0) {
        items[existsIdx] = {...items[existsIdx], name: value};
    } else if (renameTarget) {
        items.push({...renameTarget, name: value});
    }
    closeModal(modalRename);
    renameTarget = null;
    render();
}

// Preview modal
async function openPreview(item) {
    previewTarget = item;
    const titleEl = document.getElementById("modal-preview-title");
    titleEl.textContent = "Aperçu : " + item.name;
    previewContainer.innerHTML = "";
    if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
        previewUrl = null;
    }

    if (item.type === "file" && item.file) {
        const f = item.file;
        if (f.type.startsWith("image/")) {
            previewUrl = URL.createObjectURL(f);
            const img = document.createElement("img");
            img.className = "preview__img";
            img.alt = "Aperçu de " + item.name;
            img.src = previewUrl;
            previewContainer.appendChild(img);
        } else if (isTextLike(f.name, f.type)) {
            try {
                const text = await f.text();
                const pre = document.createElement("pre");
                pre.className = "preview__pre";
                pre.textContent = text;
                previewContainer.appendChild(pre);
            } catch {
                previewContainer.textContent =
                    "Impossible d’afficher l’aperçu.";
            }
        } else {
            previewContainer.textContent =
                "Aucun aperçu disponible pour ce type de fichier.";
        }
    }
    openModal(modalPreview);
}

// Modal generic
function openModal(modal, onOpen) {
    modal.classList.add("is-open");
    modal.removeAttribute("aria-hidden");
    document.body.style.overflow = "hidden";
    if (typeof onOpen === "function") onOpen();
}

function closeModal(modal) {
    modalFolderElement.classList.remove("is-open");
    modal.classList.remove("is-open");
    modalFolderElement.removeAttribute("aria-hidden");
    modal.setAttribute("aria-hidden", "true");
    document.body.style.overflow = "";
    if (modal === modalPreview) {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
        previewTarget = null;
    }
    if (modal === modalRename) {
        renameTarget = null;
    }
}

// Event bindings
// btnUploadSidebar.addEventListener("click", handleUploadClick);
//btnUploadToolbar.addEventListener("click", handleUploadClick);
//btnUploadEmpty.addEventListener("click", handleUploadClick);


const newFolderButtons = [
    btnNewFolder,
    btnNewFolderTb,
    btnNewFolderEmpty,
].filter(Boolean);
newFolderButtons.forEach((btn) =>
    btn.addEventListener("click", newFolder)
);


// Close modal on overlay or [data-close-modal]
document.addEventListener("click", (e) => {
    const target = e.target;
    if (!(target instanceof Element)) return;
    if (target.hasAttribute("data-close-modal")) {
        const dialog = target.closest(".modal");
        if (dialog) closeModal(dialog);
    }
});
// Esc to close top-most modal
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        if (modalPreview.classList.contains("is-open"))
            closeModal(modalPreview);
        else if (modalRename.classList.contains("is-open"))
            closeModal(modalRename);
    }
});


