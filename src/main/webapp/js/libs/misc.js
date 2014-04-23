//* miscellaneous scripts for registration *//


function clearText(field){
 
    if (field.defaultValue == field.value) field.value = '';
    else if (field.value == '') field.value = field.defaultValue;
 
}

	function windowpop(url, width, height) {
		event.preventDefault();
		var leftPosition, topPosition;
		//Allow for borders.
		leftPosition = (window.screen.width / 2) - ((width / 2) + 10);
		//Allow for title and status bars.
		topPosition = (window.screen.height / 2) - ((height / 2) + 50);
		//Open the window.
		window.open(url, "Window2", "status=no,height=" + height + ",width=" + width + ",resizable=yes,left=" + leftPosition + ",top=" + topPosition + ",screenX=" + leftPosition + ",screenY=" + topPosition + ",toolbar=no,menubar=no,scrollbars=no,location=no,directories=no");
	}