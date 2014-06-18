package net.respectnetwork.csp.application.dao;

import net.respectnetwork.csp.application.constants.Status;
import net.respectnetwork.csp.application.model.LicenseKeyModel;

public interface LicenseKeyDAO {
    public LicenseKeyModel get(String cspCloudNumber, String userCloudNumber)
            throws DAOException;

    public Status insert(LicenseKeyModel licenceKeyModel) throws DAOException;
}
