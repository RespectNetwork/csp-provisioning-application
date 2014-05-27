package net.respectnetwork.csp.application.exception;

public class PasswordValidationException extends CSPException {
    private static final long serialVersionUID = 100L;

    public PasswordValidationException(String msg) {
        super(CSPException.VALIDATION, msg);
    }

    public PasswordValidationException(Throwable t) {
        super(CSPException.VALIDATION, t);

    }

    public PasswordValidationException(String msg, Throwable t) {
        super(CSPException.VALIDATION, msg, t);

    }

    public PasswordValidationException(int faultCode, String msg) {
        super(CSPException.VALIDATION, faultCode, msg);
    }

    public PasswordValidationException(int faultCode, Throwable t) {
        super(CSPException.VALIDATION, faultCode, t);

    }

    public PasswordValidationException(int faultCode, String msg, Throwable t) {
        super(CSPException.VALIDATION, faultCode, msg, t);
    }
}
