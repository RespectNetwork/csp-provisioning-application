package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.CSPModel;

public interface CSPDAO
{
	public List<CSPModel> list() throws DAOException;
	public CSPModel get( String cspCloudName ) throws DAOException;
}
