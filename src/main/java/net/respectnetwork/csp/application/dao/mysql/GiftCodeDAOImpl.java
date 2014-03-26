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

import net.respectnetwork.csp.application.model.GiftCodeModel;
import net.respectnetwork.csp.application.dao.GiftCodeDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class GiftCodeDAOImpl extends BaseDAOImpl implements GiftCodeDAO
{
	private static final Logger logger = LoggerFactory.getLogger(GiftCodeDAOImpl.class);

	public GiftCodeDAOImpl()
	{
		super();
		logger.info("GiftCodeDAOImpl() created");
	}

	private GiftCodeModel get( ResultSet rset ) throws SQLException
	{
		GiftCodeModel gift = new GiftCodeModel();

		gift.setGiftCodeId (rset.getString   (1));
		gift.setInviteId   (rset.getString   (2));
		gift.setPaymentId  (rset.getString   (3));
		gift.setTimeCreated(rset.getTimestamp(4));

		return gift;
	}

	public List<GiftCodeModel> list( String inviteId ) throws DAOException
	{
		logger.info("list() " + inviteId);

		List<GiftCodeModel> rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		ResultSet           rset = null;
		String              sql  = null;

		try
		{
			sql = "select giftcode_id, invite_id, payment_id, time_created from giftcode where invite_id = ?";
			logger.info(sql + " : " + inviteId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviteId);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				GiftCodeModel gift = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<GiftCodeModel>();
				}
				rtn.add(gift);
				logger.info(gift.toString());
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
			logger.error("No GiftCode found");
		}
		else
		{
			logger.error("GiftCode found = " + rtn.size());
		}
		return rtn;
	}

	public GiftCodeModel get( String giftCodeId ) throws DAOException
	{
		logger.info("get() - " + giftCodeId);

		GiftCodeModel     rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select giftcode_id, invite_id, payment_id, time_created from giftcode where giftcode_id = ?";
			logger.info(sql + " : " + giftCodeId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, giftCodeId);
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
			logger.error("GiftCode not found - " + giftCodeId);
		}
		return rtn;
	}

	public GiftCodeModel insert( GiftCodeModel giftCode ) throws DAOException
	{
		logger.info("insert() - " + giftCode);

		GiftCodeModel       rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		String              sql  = null;

		try
		{
			sql = "insert into giftcode (giftcode_id, invite_id, payment_id, time_created) values (?, ?, ?, now())";
			logger.info(sql + " : " + giftCode);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, giftCode.getGiftCodeId());
			stmt.setString(2, giftCode.getInviteId());
			stmt.setString(3, giftCode.getPaymentId());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + giftCode + " return " + rows + " rows ");
			}
			else
			{
				rtn = giftCode;
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
			logger.error("GiftCode insert failed - " + giftCode);
		}
		return rtn;
	}
}
