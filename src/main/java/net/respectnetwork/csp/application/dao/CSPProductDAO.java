package net.respectnetwork.csp.application.dao;

import net.respectnetwork.csp.application.model.CSPProductModel;

/**
 * @author psharma2
 * 
 */
public interface CSPProductDAO {
    public CSPProductModel get(String productId, String cspName)
            throws DAOException;
}
