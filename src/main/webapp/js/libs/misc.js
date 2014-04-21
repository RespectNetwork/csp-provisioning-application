//* miscellaneous scripts for registration *//


function clearText(field){
 
    if (field.defaultValue == field.value) field.value = '';
    else if (field.value == '') field.value = field.defaultValue;
 
}
