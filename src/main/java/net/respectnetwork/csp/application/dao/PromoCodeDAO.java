package net.respectnetwork.csp.application.dao;

import net.respectnetwork.csp.application.model.PromoCodeModel;

public interface PromoCodeDAO
{
	
	public PromoCodeModel get( String promoCode ) throws DAOException;
	
}
