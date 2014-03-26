package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.GiftCodeModel;

public interface GiftCodeDAO
{
	public List<GiftCodeModel> list( String inviteId ) throws DAOException;
	public GiftCodeModel get( String giftCodeId ) throws DAOException;
	public GiftCodeModel insert( GiftCodeModel giftCode ) throws DAOException;
}
