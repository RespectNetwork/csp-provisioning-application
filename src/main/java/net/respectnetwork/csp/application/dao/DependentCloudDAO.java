package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.DependentCloudModel;

public interface DependentCloudDAO
{
	public List<DependentCloudModel> list( String guardianCloudName ) throws DAOException;
	public DependentCloudModel insert( DependentCloudModel dependentCloud ) throws DAOException;
}
