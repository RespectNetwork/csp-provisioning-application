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
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.SignupInfoModel;
import net.respectnetwork.csp.application.dao.CSPDAO;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.SignupInfoDAO;

public class SignupInfoDAOImpl extends BaseDAOImpl implements SignupInfoDAO
{
	private static final Logger logger = LoggerFactory.getLogger(SignupInfoDAOImpl.class);

	public SignupInfoDAOImpl()
	{
		super();
		logger.info("SignupInfoDAOImpl() created");
	}

	private SignupInfoModel get( ResultSet rset ) throws SQLException
	{
	   SignupInfoModel info = new SignupInfoModel();

	   info.setCloudName(rset.getString    (1));
	   info.setEmail(rset.getString    (2));
	   info.setPhone(rset.getString    (3));
	   info.setPaymentType(rset.getString    (4));
	   info.setPaymentRefId(rset.getString    (5));

		return info;
	}

	public List<SignupInfoModel> list() throws DAOException
	{
		logger.info("list()");

		List<SignupInfoModel>    rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select cloudname , email , phone , payment_type, payment_ref_id from signup_info";
			logger.info(sql);
			stmt = conn.prepareStatement(sql);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
			   SignupInfoModel info = this.get(rset);
				if( rtn == null )
				{
					rtn = new ArrayList<SignupInfoModel>();
				}
				rtn.add(info);
				logger.info(info.toString());
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
			logger.error("No SignupInfo found");
		}
		else
		{
			logger.info("SignupInfo found = " + rtn.size());
		}
		return rtn;
	}

	public SignupInfoModel get( String cloudName ) throws DAOException
	{
		logger.info("get() - " + cloudName);

		SignupInfoModel          rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select cloudname, email , phone, payment_type, payment_ref_id from signup_info where cloudname = ?";
			logger.info(sql + " : " + cloudName);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, cloudName);
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
			logger.error("CSP not found - " + cloudName);
		}
		return rtn;
	}
	public SignupInfoModel insert( SignupInfoModel signupInfo ) throws DAOException
   {
      logger.info("insert() - " + signupInfo);

      SignupInfoModel         rtn  = null;
      Connection          conn = this.getConnection();
      PreparedStatement   stmt = null;
      String              sql  = null;

      try
      {
         sql = "insert into signup_info (cloudname , email , phone, payment_type, payment_ref_id, time_created) values (?, ?, ?, ?, ?, now())";
         logger.info(sql + " : " + signupInfo);
         stmt = conn.prepareStatement(sql);
         stmt.setString(1, signupInfo.getCloudName());
         stmt.setString(2, signupInfo.getEmail());
         stmt.setString(3, signupInfo.getPhone());
         stmt.setString(4, signupInfo.getPaymentType());
         stmt.setString(5, signupInfo.getPaymentRefId());

         int rows = stmt.executeUpdate();
         if( rows != 1 )
         {
            logger.error(sql + " : " + signupInfo + " return " + rows + " rows ");
         }
         else
         {
            rtn = signupInfo;
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
         logger.error("Invite insert failed - " + signupInfo);
      }
      return rtn;
   }
}
