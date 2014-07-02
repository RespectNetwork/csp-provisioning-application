package net.respectnetwork.csp.application.constants;

public enum CSPErrorsEnum {
    
    VE_INVALID_CURRENT_PASSWORD(1000, "You have provided an incorrect current password."),
    VE_INVALID_PASSWORD_FORMAT(1001, "Invalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc."),
    VE_INVALID_CLOUD_NAME(1002, "The Cloud Name provided is not valid."),
    VE_INVALID_NOT_REGISTERED_EMAIL(1003, "The email address provided is not registered with this cloud name."),
    VE_INVALID_NOT_REGISTERED_PHONE(1004, "The phone number provided is not registered with this cloud name."),
    VE_INVALID_CLOUD_NAME_FORMAT(1005, "Personal cloud names must start with an = sign and business cloud names with a + sign. After that, they may contain up to 64 characters in any supported character set, plus dots or dashes. They may not start or end with a dot or a dash nor contain consecutive dots or dashes. The supported character sets include Latin (which covers many European languages such as German, Swedish and Spanish), Chinese, Japanese, and Korean."),
    
    VE_ERROR_CLOUD_NAME_NOT_EXIST(1101, "The Cloud Name provided does not exist."),
    
    VE_ERROR_CLOUD_NAME_PASSWORD_NOT_EXIST(1102, "Invalid CloudName/Paswword."),
    
    
    VE_ERROR_SYSTEM_ERROR(2000, "Currently we are not able to fulfill your request due to some system error. Please try again later.");
    
    private int code;
    private String message;

    
    /**
     * @param code
     * @param message
     */
    CSPErrorsEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 
     * @return code
     */
    public int code() {
        return code;
    }

    /**
     * @return message
     */
    public String message() {
        return message;
    }

    /**
     * @return code : message
     */
    public String toString() {
        return code + ": " + message;
    }
}
