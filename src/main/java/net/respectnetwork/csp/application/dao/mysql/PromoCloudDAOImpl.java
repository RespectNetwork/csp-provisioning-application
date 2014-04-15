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
import net.respectnetwork.csp.application.model.PromoCloudModel;
import net.respectnetwork.csp.application.dao.InviteDAO;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.PromoCloudDAO;

public class PromoCloudDAOImpl extends BaseDAOImpl implements PromoCloudDAO
{
	private static final Logger logger = LoggerFactory.getLogger(PromoCloudDAOImpl.class);

	public PromoCloudDAOImpl()
	{
		super();
		logger.info("PromoCloudDAOImpl() created");
	}

	private PromoCloudModel get( ResultSet rset ) throws SQLException
	{
	   PromoCloudModel rtn = new PromoCloudModel();

		rtn.setPromo_id(rset.getString   (1));
		rtn.setCloudname(rset.getString   (2));
		rtn.setCreation_date(rset.getTimestamp   (3));
		rtn.setCsp_cloudname(rset.getString   (4));
		return rtn;
	}

	
	

	public PromoCloudModel insert( PromoCloudModel promoCloud ) throws DAOException
	{
		logger.info("insert() - " + promoCloud);

		PromoCloudModel         rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		String              sql  = null;

		try
		{
			sql = "insert into promo_cloud (promo_id, cloudname, creation_date, csp_cloudname) values (?, ?, now(), ?)";
			logger.info(sql + " : " + sql);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, promoCloud.getPromo_id());
			stmt.setString(2, promoCloud.getCloudname());
			
			stmt.setString(3, promoCloud.getCsp_cloudname());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + promoCloud + " return " + rows + " rows ");
			}
			else
			{
				rtn = promoCloud;
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
			logger.error("PromoCloud insert failed - " + promoCloud);
		}
		return rtn;
	}
}
