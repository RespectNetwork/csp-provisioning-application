package net.respectnetwork.csp.application.manager;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.LicenseKeyModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import xdi2.discovery.XDIDiscoveryClient;

public class PersonalCloudManager {

    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(PersonalCloudManager.class);

    private XDIDiscoveryClient personalCloudDiscoveryClient;

    public XDIDiscoveryClient getPersonalCloudDiscoveryClient() {
        return personalCloudDiscoveryClient;
    }

    @Autowired
    public void setPersonalCloudDiscoveryClient(
            XDIDiscoveryClient personalCloudDiscoveryClient) {
        this.personalCloudDiscoveryClient = personalCloudDiscoveryClient;
    }

    public LicenseKeyModel getLicenceKey(String cspCloudNumber,
            String userCloudNumber) {
        logger.info("Fetch licence key for user : " + userCloudNumber);
        DAOFactory dao = DAOFactory.getInstance();
        LicenseKeyModel licenceKeyModel = new LicenseKeyModel();
        try {
            licenceKeyModel = dao.getLicenseKeyDAO().get(cspCloudNumber,
                    userCloudNumber);
        } catch (DAOException e) {
            logger.error("Problem fetching record for licence for user: "
                    + userCloudNumber.toString());
            logger.error("DB Exception : " + e.getMessage());
        }
        return licenceKeyModel;
    }

}
