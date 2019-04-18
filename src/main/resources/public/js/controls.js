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
        var currentCitations = current_graph.citations[selected.id] !== undefined ? current_graph.citations[selected.id].length : 0;
        var maxCitations = data.all_graph.citations[selected.id] !== undefined ? data.all_graph.citations[selected.id].length : 0;
        expandCitations.disabled = !(currentCitations < maxCitations);
        contractCitations.disabled = !(currentCitations > 0);
    } else {
        citationButtons.style.display = 'none';
        collaborationButtons.style.display = '';
        var currentCollaborations = current_graph.collaborations[selected.id] !== undefined ? current_graph.collaborations[selected.id].length : 0;
        var maxCollaborations = data.all_graph.collaborations[selected.id] !== undefined ? data.all_graph.collaborations[selected.id].length : 0;
        expandCollaborations.disabled = !(currentCollaborations < maxCollaborations);
        contractCollaborations.disabled = !(currentCollaborations > 0);
    }
    var currentPublications = current_graph.publications[selected.id] !== undefined ? current_graph.publications[selected.id].length : 0;
    var maxPublications = data.all_graph.publications[selected.id] !== undefined ? data.all_graph.publications[selected.id].length : 0;
    expandPublications.disabled = !(currentPublications < maxPublications);
    contractPublications.disabled = !(currentPublications > 0);
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
    addLinks(data.all_graph.citations[selected.id]);
    updateButtons();
}

function contractCitations() {
    removeLinks(current_graph.citations[selected.id]);
    updateButtons();
}

function expandCollaborations() {
    addLinks(data.all_graph.collaborations[selected.id]);
    updateButtons();
}

function contractCollaborations() {
    removeLinks(current_graph.collaborations[selected.id]);
    updateButtons();
}

function expandPublications() {
    addLinks(data.all_graph.publications[selected.id]);
    updateButtons();
}

function contractPublications() {
    removeLinks(current_graph.publications[selected.id]);
    updateButtons();
}