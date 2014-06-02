package net.respectnetwork.csp.application.exception;

import java.io.Serializable;

public class CSPProValidationException extends CSPException implements
        Serializable {

    private static final long serialVersionUID = 100L;

    public CSPProValidationException(String msg) {
        super(CSPException.VALIDATION, msg);
    }

    public CSPProValidationException(Throwable t) {
        super(CSPException.VALIDATION, t);

    }

    public CSPProValidationException(String msg, Throwable t) {
        super(CSPException.VALIDATION, msg, t);

    }

    public CSPProValidationException(int faultCode, String msg) {
        super(CSPException.VALIDATION, faultCode, msg);
    }

    public CSPProValidationException(int faultCode, Throwable t) {
        super(CSPException.VALIDATION, faultCode, t);

    }

    public CSPProValidationException(int faultCode, String msg, Throwable t) {
        super(CSPException.VALIDATION, faultCode, msg, t);
    }

}
