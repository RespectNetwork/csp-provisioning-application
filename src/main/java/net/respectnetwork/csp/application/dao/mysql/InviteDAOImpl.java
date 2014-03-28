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

import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.dao.InviteDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class InviteDAOImpl extends BaseDAOImpl implements InviteDAO
{
	private static final Logger logger = LoggerFactory.getLogger(InviteDAOImpl.class);

	public InviteDAOImpl()
	{
		super();
		logger.info("InviteDAOImpl() created");
	}

	private InviteModel get( ResultSet rset ) throws SQLException
	{
		InviteModel inv = new InviteModel();

		inv.setInviteId           (rset.getString   (1));
		inv.setCspCloudName       (rset.getString   (2));
		inv.setInviterCloudName   (rset.getString   (3));
		inv.setInvitedEmailAddress(rset.getString   (4));
		inv.setEmailSubject       (rset.getString   (5));
		inv.setEmailMessage       (rset.getString   (6));
		inv.setTimeCreated        (rset.getTimestamp(7));

		return inv;
	}

	public List<InviteModel> list( String inviterCloudName ) throws DAOException
	{
		logger.info("list() " + inviterCloudName);

		List<InviteModel> rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select invite_id, csp_cloudname, inviter_cloudname, invited_email_address, email_subject, email_message, time_created from invite where inviter_cloudname = ?";
			logger.info(sql + " : " + inviterCloudName);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviterCloudName);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				InviteModel inv = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<InviteModel>();
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
			logger.error("No Invite found");
		}
		else
		{
			logger.error("Invite found = " + rtn.size());
		}
		return rtn;
	}

	public List<InviteModel> listGroupByInvited( String inviterCloudName ) throws DAOException
	{
		logger.info("listlistGroupByInvited() " + inviterCloudName);

		List<InviteModel> rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select lower(invited_email_address), max(time_created), (select count(*) from giftcode where invite_id = p.invite_id) from invite p where inviter_cloudname = ? group by lower(invited_email_address) order by lower(invited_email_address)";

			logger.info(sql + " : " + inviterCloudName);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviterCloudName);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				InviteModel inv = new InviteModel();
				inv.setInvitedEmailAddress(rset.getString   (1));
				inv.setTimeCreated        (rset.getTimestamp(2));
				inv.setGiftCardCount      (rset.getInt      (3));
				if( rtn == null )
				{
					rtn = new ArrayList<InviteModel>();
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
			logger.error("No Invite found");
		}
		else
		{
			logger.error("Invite found = " + rtn.size());
		}
		return rtn;
	}

	public InviteModel get( String inviteId ) throws DAOException
	{
		logger.info("get() - " + inviteId);

		InviteModel       rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select invite_id, csp_cloudname, inviter_cloudname, invited_email_address, email_subject, email_message, time_created from invite where invite_id = ?";
			logger.info(sql + " : " + inviteId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviteId);
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
			logger.error("Invite not found - " + inviteId);
		}
		return rtn;
	}

	public InviteModel insert( InviteModel invite ) throws DAOException
	{
		logger.info("insert() - " + invite);

		InviteModel         rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		String              sql  = null;

		try
		{
			sql = "insert into invite (invite_id, csp_cloudname, inviter_cloudname, invited_email_address, email_subject, email_message, time_created) values (?, ?, ?, ?, ?, ?, now())";
			logger.info(sql + " : " + invite);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, invite.getInviteId());
			stmt.setString(2, invite.getCspCloudName());
			stmt.setString(3, invite.getInviterCloudName());
			stmt.setString(4, invite.getInvitedEmailAddress());
			stmt.setString(5, invite.getEmailSubject());
			stmt.setString(6, invite.getEmailMessage());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + invite + " return " + rows + " rows ");
			}
			else
			{
				rtn = invite;
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
			logger.error("Invite insert failed - " + invite);
		}
		return rtn;
	}
}
