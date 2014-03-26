package net.respectnetwork.csp.application.dao;

import java.util.List;

import net.respectnetwork.csp.application.model.PaymentModel;

public interface PaymentDAO
{
	public PaymentModel get( String paymentId ) throws DAOException;
	public PaymentModel insert( PaymentModel payment ) throws DAOException;
}
