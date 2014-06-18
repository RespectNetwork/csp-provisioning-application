package net.respectnetwork.csp.application.constants;

public enum Status {

    SUCCESS(0), FAILURE(1);

    int code;

    private Status(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
