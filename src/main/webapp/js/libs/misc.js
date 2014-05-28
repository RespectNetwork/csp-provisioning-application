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
	
	function validateEmail(x)
	{
		
		var atpos=x.indexOf("@");
		var dotpos=x.lastIndexOf(".");
		if (atpos<1 || dotpos<atpos+2 || dotpos+2>=x.length)
		{
		  return false;
		}
		return true;
	}
	function validatePhone(x)
	{
		return true;
	}
	function validatePassword(x)
	{
		return true;
	}

if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function (str){
        return (this.lastIndexOf(str, 0) === 0);
    };
}

function addFormValidation(formSelector, validation) {
    $(formSelector).validate(
        $.extend({
            errorClass: 'form-control-feedback',
            validClass: 'form-control-feedback',

            errorPlacement: function (label, element) {
                var group = element.closest('[class^="form-group"]');
                var secondary = group.find('[class^="secondary"]'); // if wrapped in secondary (e.g. check boxes)
                var insertAfterElement = element;
                if (secondary.length > 0) {
                    insertAfterElement = secondary;
                }
                label.insertAfter(insertAfterElement);
            },
            success: function (label) {
                label.closest('[class^="form-group"]').addClass('has-feedback');
                label.addClass('glyphicon glyphicon-ok');
            },
            highlight: function (element, errorClass, validClass) {
                $(element).closest('[class^="form-group"]').addClass('has-error').removeClass('has-success has-feedback');
                $(element.form).find("label[for=" + element.id + "]").removeClass('glyphicon glyphicon-ok');
            },
            unhighlight: function (element, errorClass, validClass) {
                $(element).closest('[class^="form-group"]').removeClass('has-error').addClass('has-success');
            }
        }, validation)
    );
}
