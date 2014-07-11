function validatePwd(pwd, dependentCounter) {
	var retVal = null;
    if(dependentCounter == undefined) {
        var isValidPassword = "#isValidPassword";
        var confirmpwd = "confirmPassword";
    } else {
        var isValidPassword = "#isValidPassword"+dependentCounter;
        var confirmpwd = "confirmPassword"+dependentCounter;
    }
    if(!pwd || 0 === pwd.length || pwd.length < 8) {
        retVal = "\nInvalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc.";
    }
    var letterCount = 0;
    var digitCount = 0;
    var specialCharCount = 0;
    for (var i = 0; i < pwd.length; i++) {
        if(pwd.charAt(i) >= '0' && pwd.charAt(i) <= '9') {
           digitCount++;
        }
        if((pwd.charAt(i) >= 'A' && pwd.charAt(i) <= 'Z') || ((pwd.charAt(i) >= 'a' && pwd.charAt(i) <= 'z'))) {
           letterCount++;
        }

        if((pwd.charAt(i) ==  '!') ||
              (pwd.charAt(i) ==  '@') ||
              (pwd.charAt(i) ==  '#') ||
              (pwd.charAt(i) ==  '$') ||
              (pwd.charAt(i) ==  '%') ||
              (pwd.charAt(i) ==  '^') ||
              (pwd.charAt(i) ==  '*') ||
              (pwd.charAt(i) ==  '(') ||
              (pwd.charAt(i) ==  ')') ||
              (pwd.charAt(i) ==  '_') ||
              (pwd.charAt(i) ==  '~') ||
              (pwd.charAt(i) ==  '-') ||
              (pwd.charAt(i) ==  '=') ||
              (pwd.charAt(i) ==  '\\') ||
              (pwd.charAt(i) ==  '`') ||
              (pwd.charAt(i) ==  '{') ||
              (pwd.charAt(i) ==  '}') ||
              (pwd.charAt(i) ==  '[') ||
              (pwd.charAt(i) ==  ']') ||
              (pwd.charAt(i) ==  ':') ||
              (pwd.charAt(i) ==  '"') ||
              (pwd.charAt(i) ==  ';') ||
              (pwd.charAt(i) ==  '\'') ||
              (pwd.charAt(i) ==  '<') ||
              (pwd.charAt(i) ==  '>') ||
              (pwd.charAt(i) ==  '?') ||
              (pwd.charAt(i) ==  '.') ||
              (pwd.charAt(i) ==  '&') ||
              (pwd.charAt(i) ==  '/') 
              )
        {
           specialCharCount++;
        }
        
     }
     if(!(letterCount >= 2 && digitCount >= 2 && specialCharCount >= 1)) {
        retVal = "\nInvalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc.";
     }
   	$(isValidPassword).html(retVal);
    $(isValidPassword).css('color', 'red');

    var confPass = document.getElementById(confirmpwd).value;
    if(confPass != "") {
        confirmPwd(dependentCounter);
    }
    disableEnableContinue();
}

// Function to validate password and check if password and confirm password is same or not.
function confirmPwd(dependentCounter) {
	if(dependentCounter == undefined) {
        var dependentCloudPwd = "dependentCloudPassword";
        var confirmpwd = "confirmPassword";
        var isValidConfirmPwd = "isValidConfirmPassword";
    } else {
        var dependentCloudPwd = "dependentCloudPassword"+window.mycounter;
        var confirmpwd = "confirmPassword"+window.mycounter;
        var isValidConfirmPwd = "isValidConfirmPassword"+window.mycounter;
    }

	var pass = document.getElementById(dependentCloudPwd).value
	var confPass = document.getElementById(confirmpwd).value

    if((pass != confPass) && (pass != "") && (confPass != "")) {
		   document.getElementById(isValidConfirmPwd).innerHTML = 'Please enter the same value again.';
	   } else {
		   document.getElementById(isValidConfirmPwd).innerHTML = '';
	   }
	disableEnableContinue();
}