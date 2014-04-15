package net.respectnetwork.csp.application.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.PromoCodeDAO;
import net.respectnetwork.csp.application.model.PromoCodeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromoCodeDAOImpl extends BaseDAOImpl implements PromoCodeDAO
{
	private static final Logger logger = LoggerFactory.getLogger(PromoCodeDAOImpl.class);

	public PromoCodeDAOImpl()
	{
		super();
		logger.info("PromoCodeDAOImpl() created");
	}

	private PromoCodeModel get( ResultSet rset ) throws SQLException
	{
	   PromoCodeModel promo = new PromoCodeModel();

		promo.setPromo_id(rset.getString   (1));
		promo.setStart_date(rset.getTimestamp   (2));
		promo.setEnd_date(rset.getTimestamp   (3));
		promo.setPromo_limit(rset.getInt(4));
		return promo;
	}


	public PromoCodeModel get( String promoCode  ) throws DAOException
	{
		logger.info("get() - " + promoCode );

		PromoCodeModel       rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
		   /*
		    * Select cd.promo_id , start_date , end_date , promo_limit from promo_code cd left join promo_cloud cl on ( cd.promo_id = cl.promo_id) where cd.promo_id = "NSR-IIS-MAY" 
		    * and promo_limit > (select count(*) from promo_cloud where promo_id="NSR-IIS-MAY");
		    */
			sql = "Select cd.promo_id , start_date , end_date , promo_limit from promo_code cd left join promo_cloud cl on ( cd.promo_id = cl.promo_id) where cd.promo_id = ? "
			      + "and (start_date is null or start_date < now()) and (end_date is null or end_date > now()) and promo_limit > (select count(*) from promo_cloud where promo_id= ?)";
			logger.info(sql + " : " + promoCode);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, promoCode);
			stmt.setString(2, promoCode);
			
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
			logger.error("Active Promotion not found - " + promoCode);
		}
		return rtn;
	}

	}
