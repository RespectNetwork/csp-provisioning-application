package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.GiftCodeRedemptionModel;

public interface GiftCodeRedemptionDAO
{
	public List<GiftCodeRedemptionModel> list( String inviteId ) throws DAOException;
	public GiftCodeRedemptionModel get( String giftCodeId ) throws DAOException;
	public GiftCodeRedemptionModel insert( GiftCodeRedemptionModel giftCodeRedemption ) throws DAOException;
}
