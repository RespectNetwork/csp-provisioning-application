/**
 * 
 */
package net.respectnetwork.csp.application.form;

/**
 * @author kvats
 * 
 */
public class ChangePasswordForm {

    /**
     * Current password which user want to change
     */
    private String currentPassword;

    /**
     * New password provided by user to update
     */
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ChangePasswordForm [currentPassword=" + currentPassword)
                .append(", newPassword=" + newPassword).append("]");
        return builder.toString();
    }

}
