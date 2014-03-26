package net.respectnetwork.csp.application.dao.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.csp.application.dao.DAOException;

public class BaseDAOImpl
{
	private static final Logger logger = LoggerFactory.getLogger(BaseDAOImpl.class);

	protected DataSource	dataSource;

	public BaseDAOImpl()
	{
		this.dataSource = null;
		logger.info("BaseImpl() created");
	}

	public void setDataSource( DataSource dataSource )
	{
		logger.info("Set datasource " + dataSource);
		this.dataSource = dataSource;
	}

	public DataSource getDataSource()
	{
		return this.dataSource;
	}

	public Connection getConnection() throws DAOException
	{
		logger.debug("Get database connection");
		Connection rtn = null;
		try
		{
			rtn = this.dataSource.getConnection();
		}
		catch( SQLException e )
		{
			String err = "Get database connection failed";
			logger.error(err, e);
			throw new DAOException(err, e);
		}

		logger.debug("Get database connection ok");
		return rtn;
	}

	public void closeConnection( Connection conn, Statement stmt, ResultSet rset ) throws DAOException
	{
		logger.debug("Close database connection");
		if( conn != null )
		{
			try
			{
				if( rset != null )
				{
					rset.close();
				}
				if( stmt != null )
				{
					stmt.close();
				}
				conn.close();
				conn = null;
			}
			catch( SQLException e )
			{
				String err = "Close database connection failed";
				logger.error(err, e);
				throw new DAOException(err, e);
			}
		}
		logger.debug("Close database connection ok");
	}

	public void closeConnection( Connection conn, Statement stmt ) throws DAOException
	{
		closeConnection(conn, stmt, null);
	}

	public void closeConnection( Connection conn ) throws DAOException
	{
		closeConnection(conn, null, null);
	}
}
