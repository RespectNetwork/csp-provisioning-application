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

import net.respectnetwork.csp.application.model.GiftCodeRedemptionModel;
import net.respectnetwork.csp.application.dao.GiftCodeRedemptionDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class GiftCodeRedemptionDAOImpl extends BaseDAOImpl implements GiftCodeRedemptionDAO
{
	private static final Logger logger = LoggerFactory.getLogger(GiftCodeRedemptionDAOImpl.class);

	public GiftCodeRedemptionDAOImpl()
	{
		super();
		logger.info("GiftCodeRedemptionDAOImpl() created");
	}

	private GiftCodeRedemptionModel get( ResultSet rset ) throws SQLException
	{
		GiftCodeRedemptionModel gift = new GiftCodeRedemptionModel();

		gift.setRedemptionId    (rset.getString   (1));
		gift.setGiftCodeId      (rset.getString   (2));
		gift.setCloudNameCreated(rset.getString   (3));
		gift.setTimeCreated     (rset.getTimestamp(4));

		return gift;
	}

	public List<GiftCodeRedemptionModel> list( String inviteId ) throws DAOException
	{
		logger.info("list() " + inviteId);

		List<GiftCodeRedemptionModel> rtn  = null;
		Connection                    conn = this.getConnection();
		PreparedStatement             stmt = null;
		ResultSet                     rset = null;
		String                        sql  = null;

		try
		{
			sql = "select g.redemption_id, g.giftcode_id, g.cloudname_created, g.time_created from giftcode_redemption g, giftcode i where i.invite_id = ? and i.giftcode_id = g.giftcode_id";
			logger.info(sql + " : " + inviteId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, inviteId);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				GiftCodeRedemptionModel gift = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<GiftCodeRedemptionModel>();
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
			logger.error("No GiftCodeRedemption found");
		}
		else
		{
			logger.error("GiftCodeRedemption found = " + rtn.size());
		}
		return rtn;
	}

	public GiftCodeRedemptionModel get( String giftCodeId ) throws DAOException
	{
		logger.info("get() - " + giftCodeId);

		GiftCodeRedemptionModel rtn  = null;
		Connection              conn = this.getConnection();
		PreparedStatement       stmt = null;
		ResultSet               rset = null;
		String                  sql  = null;

		try
		{
			sql = "select redemption_id, giftcode_id, cloudname_created, time_created from giftcode_redemption where giftcode_id = ?";
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
			logger.error("GiftCodeRedemption not found - " + giftCodeId);
		}
		return rtn;
	}

	public GiftCodeRedemptionModel insert( GiftCodeRedemptionModel giftCodeRedemption ) throws DAOException
	{
		logger.info("insert() - " + giftCodeRedemption);

		GiftCodeRedemptionModel rtn  = null;
		Connection              conn = this.getConnection();
		PreparedStatement       stmt = null;
		String                  sql  = null;

		try
		{
			sql = "insert into giftcode_redemption (redemption_id, giftcode_id, cloudname_created, time_created) values (?, ?, ?, now())";
			logger.info(sql + " : " + giftCodeRedemption);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, giftCodeRedemption.getRedemptionId());
			stmt.setString(2, giftCodeRedemption.getGiftCodeId());
			stmt.setString(3, giftCodeRedemption.getCloudNameCreated());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + giftCodeRedemption + " return " + rows + " rows ");
			}
			else
			{
				rtn = giftCodeRedemption;
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
			logger.error("GiftCodeRedemption insert failed - " + giftCodeRedemption);
		}
		return rtn;
	}
}
