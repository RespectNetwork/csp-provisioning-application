package net.respectnetwork.csp.application.dao;

import net.respectnetwork.csp.application.model.CSPCostOverrideModel;

public interface CSPCostOverrideDAO
{
	public CSPCostOverrideModel get(String cspCloudName, String phoneNumber) throws DAOException;
}
