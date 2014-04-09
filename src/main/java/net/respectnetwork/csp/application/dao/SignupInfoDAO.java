package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.SignupInfoModel;

public interface SignupInfoDAO
{
	public List<SignupInfoModel> list() throws DAOException;
	public SignupInfoModel get( String cloudName ) throws DAOException;
	public SignupInfoModel insert( SignupInfoModel signupInfo ) throws DAOException;
}
