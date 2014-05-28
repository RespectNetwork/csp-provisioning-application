/**
 * 
 */
package net.respectnetwork.csp.application.exception;

import java.io.Serializable;

import net.respectnetwork.csp.application.constants.CSPErrorsEnum;

/**
 * @author kvats
 * 
 */
public class CSPException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String VALIDATION = "VALIDATION";

    private String level;
    private String message;
    private Throwable throwable;
    private int faultCode;

    public CSPException() {
        super();
    }

    public CSPException(String message) {
        super(message);
        this.message = message;
    }

    public CSPException(String level, String message, Throwable tr) {
        super(message);
        this.level = level;
        this.message = message;
        this.throwable = tr;
    }

    public CSPException(String level, String message) {
        super(message);
        this.level = level;
        this.message = message;
    }

    public CSPException(String level, Throwable tr) {
        super(tr.getMessage());
        this.level = level;
        this.throwable = tr;
    }

    public CSPException(String level, int faultCode, String message,
            Throwable tr) {
        super(message);
        this.level = level;
        this.faultCode = faultCode;
        this.message = message;
        this.throwable = tr;
    }

    public CSPException(String level, int faultCode, String message) {
        super(message);
        this.level = level;
        this.message = message;
        this.faultCode = faultCode;
    }

    public CSPException(String level, CSPErrorsEnum cspErrorsEnum, Throwable tr) {
        super(cspErrorsEnum.message());
        this.level = level;
        this.faultCode = cspErrorsEnum.code();
        this.message = cspErrorsEnum.message();
        this.throwable = tr;
    }

    public CSPException(String level, CSPErrorsEnum cspErrorsEnum) {
        super(cspErrorsEnum.message());
        this.level = level;
        this.message = cspErrorsEnum.message();
        this.faultCode = cspErrorsEnum.code();
    }

    public CSPException(String level, int faultCode, Throwable tr) {
        super(tr.getMessage());
        this.level = level;
        this.faultCode = faultCode;
        this.throwable = tr;
    }

    public String getMessage() {

        return message;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public String getLevel() {
        return this.level;
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (throwable != null) {
            recursivePrint(this);
        }
    }

    public void recursivePrint(CSPException ne) {
        Throwable thr = ne.getThrowable();
        if (thr != null) {
            thr.printStackTrace();
        }
    }

    public int getFaultCode() {
        return faultCode;
    }
}
