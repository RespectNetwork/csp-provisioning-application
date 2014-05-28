package net.respectnetwork.csp.application.model;

public class AvailabilityResponse {

    private String cloudname;
    private boolean available;
    private String error;

    public String getCloudname() {
        return cloudname;
    }

    public void setCloudname(String cloudname) {
        this.cloudname = cloudname;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
