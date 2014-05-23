package net.respectnetwork.csp.application.constants;

public enum CSPErrorsEnum {
    
    VE_INVALID_CURRENT_PASSWORD(1000, "Please provide valid current password."),
    VE_INVALID_PASSWORD_FORMAT(1001, "Invalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc."),
    VE_INVALID_CLOUD_NAME(1002, "The Cloud Name provided is not valid."),
 
    
    VE_ERROR_CLOUD_NAME_NOT_EXIST(1003, "The Cloud Name provided does not exist."),
    VE_ERROR_CLOUD_NAME_PASSWORD_NOT_EXIST(1004, "Invalid CloudName/Paswword.");
    
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
