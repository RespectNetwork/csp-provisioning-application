package net.respectnetwork.csp.application.dao;

public class DAOException extends Exception
{
	public DAOException( String message, Exception cause )
	{
		super(message, cause);
	}
}
