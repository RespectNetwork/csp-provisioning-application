package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.InviteResponseModel;

public interface InviteResponseDAO
{
	public List<InviteResponseModel> list( String inviteId ) throws DAOException;
	public InviteResponseModel get( String responseId ) throws DAOException;
	public InviteResponseModel insert( InviteResponseModel inviteResponse ) throws DAOException;
}
