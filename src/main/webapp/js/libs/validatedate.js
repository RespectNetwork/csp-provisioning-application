function validateDate(date, dependentCounter) {
    var retVal = null;
    if(dependentCounter == undefined) {
        var isValidDate = "#isValidDate";
    } else {
        var isValidDate = "#isValidDate"+dependentCounter;
    }
    // Basic check for format validity
    var validformat = /^\d{2}\/\d{2}\/\d{4}$/ 
    var returnval = false
    if (!validformat.test(date)) {
        retVal = "Invalid Date Format. Please correct and submit again.";
        document.getElementById('submit').disabled = true;
    }
    else { // Detailed check for valid date ranges
        var monthfield = date.split("/")[0]
        var dayfield = date.split("/")[1]
        var yearfield = date.split("/")[2]
        var dayobj = new Date(yearfield, monthfield - 1, dayfield)
        if ((dayobj.getMonth() + 1 != monthfield)
            || (dayobj.getDate() != dayfield)
            || (dayobj.getFullYear() != yearfield)) {
                retVal = "Invalid Day, Month, or Year range detected. Please correct and submit again.";
                document.getElementById('submit').disabled = true;
        }
    }
    $(isValidDate).html(retVal);
    $(isValidDate).css('color', 'red');

    disableEnableContinue();
}