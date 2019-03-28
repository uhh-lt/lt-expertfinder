function show(id) {
    document.getElementById(id).style.display='block';
}

function hide(id) {
    document.getElementById(id).style.display='none';
}

function toggle(id) {
    var element =  document.getElementById(id);

    if(element.style.display === 'none') {
        element.style.display = 'block';
    } else if (element.style.display === 'block') {
        element.style.display = 'none';
    }
}