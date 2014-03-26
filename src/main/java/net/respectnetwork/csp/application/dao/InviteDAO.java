package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.InviteModel;

public interface InviteDAO
{
	public List<InviteModel> list( String inviterCloudName ) throws DAOException;
	public InviteModel get( String inviteId ) throws DAOException;
	public InviteModel insert( InviteModel invite ) throws DAOException;
}
