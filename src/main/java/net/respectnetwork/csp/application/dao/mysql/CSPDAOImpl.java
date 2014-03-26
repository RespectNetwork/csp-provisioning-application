package net.respectnetwork.csp.application.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;

import java.util.List;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.dao.CSPDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class CSPDAOImpl extends BaseDAOImpl implements CSPDAO
{
	private static final Logger logger = LoggerFactory.getLogger(CSPDAOImpl.class);

	public CSPDAOImpl()
	{
		super();
		logger.info("CSPDAOImpl() created");
	}

	private CSPModel get( ResultSet rset ) throws SQLException
	{
		CSPModel csp = new CSPModel();

		csp.setCspCloudName      (rset.getString    (1));
		csp.setPaymentGatewayName(rset.getString    (2));
		csp.setPaymentUrlTemplate(rset.getString    (3));
		csp.setUsername          (rset.getString    (4));
		csp.setPassword          (rset.getString    (5));
		csp.setCostPerCloudName  (rset.getBigDecimal(6));
		csp.setCurrency          (rset.getString    (7));

		return csp;
	}

	public List<CSPModel> list() throws DAOException
	{
		logger.info("list()");

		List<CSPModel>    rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select csp_cloudname, payment_gateway_name, payment_url_template, username, password, cost_per_cloudname, currency, time_created from csp";
			logger.info(sql);
			stmt = conn.prepareStatement(sql);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				CSPModel csp = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<CSPModel>();
				}
				rtn.add(csp);
				logger.info(csp.toString());
			}
			rset.close();
			rset = null;
			stmt.close();
			stmt = null;
		}
		catch( SQLException e )
		{
			String err = "Failed to execute SQL statement - " + sql;
			logger.error(err, e);
			throw new DAOException(err, e);
		}
		finally
		{
			this.closeConnection(conn, stmt, rset);
		}
		if( rtn == null )
		{
			logger.error("No CSP found");
		}
		else
		{
			logger.error("CSP found = " + rtn.size());
		}
		return rtn;
	}

	public CSPModel get( String cspCloudName ) throws DAOException
	{
		logger.info("get() - " + cspCloudName);

		CSPModel          rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select csp_cloudname, payment_gateway_name, payment_url_template, username, password, cost_per_cloudname, currency, time_created from csp where csp_cloudname = ?";
			logger.info(sql + " : " + cspCloudName);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, cspCloudName);
			rset = stmt.executeQuery();
			if( rset.next() )
			{
				rtn = this.get(rset);
				logger.info(rtn.toString());
			}
			rset.close();
			rset = null;
			stmt.close();
			stmt = null;
		}
		catch( SQLException e )
		{
			String err = "Failed to execute SQL statement - " + sql;
			logger.error(err, e);
			throw new DAOException(err, e);
		}
		finally
		{
			this.closeConnection(conn, stmt, rset);
		}
		if( rtn == null )
		{
			logger.error("CSP not found - " + cspCloudName);
		}
		return rtn;
	}
}
