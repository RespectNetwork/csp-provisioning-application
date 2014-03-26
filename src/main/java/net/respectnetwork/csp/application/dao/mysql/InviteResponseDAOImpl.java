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

import net.respectnetwork.csp.application.model.InviteResponseModel;
import net.respectnetwork.csp.application.dao.InviteResponseDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class InviteResponseDAOImpl extends BaseDAOImpl implements InviteResponseDAO
{
	private static final Logger logger = LoggerFactory.getLogger(InviteResponseDAOImpl.class);

	public InviteResponseDAOImpl()
	{
		super();
		logger.info("InviteResponseDAOImpl() created");
	}

	private InviteResponseModel get( ResultSet rset ) throws SQLException
	{
		InviteResponseModel inv = new InviteResponseModel();

		inv.setResponseId      (rset.getString   (1));
		inv.setInviteId        (rset.getString   (2));
		inv.setPaymentId       (rset.getString   (3));
		inv.setCloudNameCreated(rset.getString   (4));
		inv.setTimeCreated     (rset.getTimestamp(5));

		return inv;
	}

	public List<InviteResponseModel> list( String inviteId ) throws DAOException
	{
		logger.info("list() " + inviteId);

		List<InviteResponseModel> rtn  = null;
		Connection                conn = this.getConnection();
		PreparedStatement         stmt = null;
		ResultSet                 rset = null;
		String                    sql  = null;

		try
		{
			sql = "select response_id, invite_id, payment_id, cloudname_created, time_created from invite_response where invite_id = ?";
			logger.info(sql + " : " + inviteId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviteId);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				InviteResponseModel inv = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<InviteResponseModel>();
				}
				rtn.add(inv);
				logger.info(inv.toString());
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
			logger.error("No InviteResponse found");
		}
		else
		{
			logger.error("InviteResponse found = " + rtn.size());
		}
		return rtn;
	}

	public InviteResponseModel get( String responseId ) throws DAOException
	{
		logger.info("get() - " + responseId);

		InviteResponseModel rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		ResultSet           rset = null;
		String              sql  = null;

		try
		{
			sql = "select response_id, invite_id, payment_id, cloudname_created, time_created from invite_response where response_id = ?";
			logger.info(sql + " : " + responseId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, responseId);
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
			logger.error("InviteResponse not found - " + responseId);
		}
		return rtn;
	}

	public InviteResponseModel insert( InviteResponseModel inviteResponse ) throws DAOException
	{
		logger.info("insert() - " + inviteResponse);

		InviteResponseModel rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		String              sql  = null;

		try
		{
			sql = "insert into invite_response (response_id, invite_id, payment_id, cloudname_created, time_created) values (?, ?, ?, ?, now())";
			logger.info(sql + " : " + inviteResponse);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviteResponse.getResponseId());
			stmt.setString(2, inviteResponse.getInviteId());
			stmt.setString(3, inviteResponse.getPaymentId());
			stmt.setString(4, inviteResponse.getCloudNameCreated());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + inviteResponse + " return " + rows + " rows ");
			}
			else
			{
				rtn = inviteResponse;
			}
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
			this.closeConnection(conn, stmt);
		}
		if( rtn == null )
		{
			logger.error("InviteResponse insert failed - " + inviteResponse);
		}
		return rtn;
	}
}
