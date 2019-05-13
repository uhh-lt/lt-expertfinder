window.addEventListener('resize', fixFooter);
function fixFooter() {
    var height = window.innerHeight
        || document.documentElement.clientHeight
        || document.body.clientHeight;
    var body = document.body;
    var bodyHeight = Math.max( body.clientHeight, body.offsetHeight);
    var footer = document.getElementsByTagName("footer")[0];

    console.log("WINDOW:" + height + "DOCUMENT" + bodyHeight);

    if((bodyHeight + 60) > height) {
        footer.classList.replace("myfooter2", "myfooter");
    } else {
        footer.classList.replace("myfooter", "myfooter2");
    }
}