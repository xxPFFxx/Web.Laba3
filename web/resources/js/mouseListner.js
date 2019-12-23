function isValidR(){
    return true;
}
function getR(){
    for(let i = 0; i < document.getElementsByName("myForm:param-r").length; i++){
        if(document.getElementsByName("myForm:param-r")[i].checked){
            return document.getElementsByName("myForm:param-r")[i].value;
        }
    }
}
document.getElementById("zoneCanvas").addEventListener("click", function (e) {
    if(isValidR()) {
        const x = (e.x - document.getElementById("zoneCanvas").getBoundingClientRect().left - 180) / (30);
        const y = (e.y - document.getElementById("zoneCanvas").getBoundingClientRect().top - 180) / (-30);
        const r = getR();
        onPointClicked(x,y,r);
    }
});
function updateR() {
    let R = getR();
    updateRDB([{name:'dbR', value: R}]);
}

