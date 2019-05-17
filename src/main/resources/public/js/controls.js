var selected = "";
var selectedGroup = -1;

var profileUrl = "";

function closeControls() {
    handleControls(selected);
}

function handleControls(d) {
    if(selected.id === d.id) {
        select(d.id, false);
        selected = {};
        selectedGroup = -1;

        hide("controls");
    } else {
        select(selected.id, false);
        selected = d;
        selectedGroup = d.group;
        select(d.id, true);

        updateControls();
        show("controls");
    }
}

function updateControls() {
    var title = document.getElementById("control-title");
    var subtitle = document.getElementById("control-subtitle");
    var link = document.getElementById("control-link");
    var publicationsname = document.getElementById("publications-name");


    if(profileUrl === "")
        profileUrl = link.getAttribute("href");

    if(selected.realgroup === 2) {
        link.setAttribute("href", "http://aclweb.org/anthology/" + selected.id + ".pdf");
        publicationsname.textContent = "Authors:"
    } else {
        link.setAttribute("href", profileUrl + "/" + selected.id);
        publicationsname.textContent = "Documents:"
    }
    title.textContent = selected.realgroup == 2 ? selected.id : selected.description;
    subtitle.textContent = selected.realgroup == 2 ? selected.description : "";
    updateButtons();
}

function updateButtons() {
    var citationButtons = document.getElementById("control-citations");
    var collaborationButtons = document.getElementById("control-collaborations");
    var expandCitations = document.getElementById("expand-citations");
    var contractCitations = document.getElementById("contract-citations");
    var expandPublications = document.getElementById("expand-publications");
    var contractPublications = document.getElementById("contract-publications");
    var expandCollaborations = document.getElementById("expand-collaborations");
    var contractCollaborations = document.getElementById("contract-collaborations");

    if(selected.realgroup === 2) {
        citationButtons.style.display = '';
        collaborationButtons.style.display = 'none';
        var initBias = init_graph.citations[selected.id] !== undefined ? init_graph.citations[selected.id].length : 0;
        var currentCitations = current_graph.citations[selected.id] !== undefined ? current_graph.citations[selected.id].length : 0;
        var maxCitations = data.all_graph.citations[selected.id] !== undefined ? data.all_graph.citations[selected.id].length : 0;
        expandCitations.disabled = !(currentCitations < maxCitations);
        contractCitations.disabled = !((currentCitations - initBias) > 0);
        if((selected.links === 1 && currentCitations === 1) || (memoryOther[selected.id] !== undefined && memoryOther[selected.id].length === currentCitations)) {
            contractCitations.firstChild.classList.replace("fa-minus", "fa-trash")
        } else {
            contractCitations.firstChild.classList.replace("fa-trash", "fa-minus")
        }
    } else {
        citationButtons.style.display = 'none';
        collaborationButtons.style.display = '';
        var initBias = init_graph.collaborations[selected.id] !== undefined ? init_graph.collaborations[selected.id].length : 0;
        var currentCollaborations = current_graph.collaborations[selected.id] !== undefined ? current_graph.collaborations[selected.id].length : 0;
        var maxCollaborations = data.all_graph.collaborations[selected.id] !== undefined ? data.all_graph.collaborations[selected.id].length : 0;
        expandCollaborations.disabled = !(currentCollaborations < maxCollaborations);
        contractCollaborations.disabled = !((currentCollaborations - initBias) > 0);
        if((selected.links === 1 && currentCollaborations === 1) || (memoryOther[selected.id] !== undefined && memoryOther[selected.id].length === currentCollaborations)) {
            contractCollaborations.firstChild.classList.replace("fa-minus", "fa-trash")
        } else {
            contractCollaborations.firstChild.classList.replace("fa-trash", "fa-minus")
        }
    }
    var initBias = init_graph.publications[selected.id] !== undefined ? init_graph.publications[selected.id].length : 0;
    var currentPublications = current_graph.publications[selected.id] !== undefined ? current_graph.publications[selected.id].length : 0;
    var maxPublications = data.all_graph.publications[selected.id] !== undefined ? data.all_graph.publications[selected.id].length : 0;
    expandPublications.disabled = !(currentPublications < maxPublications);
    contractPublications.disabled = !((currentPublications - initBias) > 0);

    if((selected.links === 1 && currentPublications === 1) || (memoryPublications[selected.id] !== undefined && memoryPublications[selected.id].length === currentPublications)) {
        contractPublications.firstChild.classList.replace("fa-minus", "fa-trash")
    } else {
        contractPublications.firstChild.classList.replace("fa-trash", "fa-minus")
    }
}

function select(id, selected) {
    var x = document.getElementById(id);
    if(x !== null) {
        if(selected) {
            x.classList.add("selected");
        } else {
            x.classList.remove("selected");
        }
    }
}

function expandCitations() {
    addLinks(selected, data.all_graph.citations[selected.id], true);
    updateButtons();
}

function contractCitations() {
    removeLinks(selected, current_graph.citations[selected.id], true);
    updateButtons();
}

function expandCollaborations() {
    addLinks(selected, data.all_graph.collaborations[selected.id], true);
    updateButtons();
}

function contractCollaborations() {
    removeLinks(selected, current_graph.collaborations[selected.id], true);
    updateButtons();
}

function expandPublications() {
    addLinks(selected, data.all_graph.publications[selected.id], false);
    updateButtons();
}

function contractPublications() {
    removeLinks(selected, current_graph.publications[selected.id], false);
    updateButtons();
}