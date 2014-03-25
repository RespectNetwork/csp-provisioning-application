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

import net.respectnetwork.csp.application.model.DependentCloudModel;
import net.respectnetwork.csp.application.dao.DependentCloudDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class DependentCloudDAOImpl extends BaseDAOImpl implements DependentCloudDAO
{
	private static final Logger logger = LoggerFactory.getLogger(DependentCloudDAOImpl.class);

	public DependentCloudDAOImpl()
	{
		super();
		logger.info("DependentCloudDAOImpl() created");
	}

	public List<DependentCloudModel> list( String guardianCloudName ) throws DAOException
	{
		logger.info("list() " + guardianCloudName);

		List<DependentCloudModel>    rtn  = null;
		Connection                   conn = this.getConnection();
		PreparedStatement            stmt = null;
		ResultSet                    rset = null;
		String                       sql  = null;

		try
		{
			sql = "select guardian_cloudname, dependent_cloudname, payment_id, time_created from dependent_cloud where guardian_cloudname = ?";
			logger.info(sql + " : " + guardianCloudName);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, guardianCloudName);
			rset = stmt.executeQuery();
			while( rset.next() )
			{
				DependentCloudModel dep = new DependentCloudModel();
				dep.setGuardianCloudName (rset.getString    (1));
				dep.setDependentCloudName(rset.getString    (2));
				dep.setPaymentId         (rset.getString    (3));
				dep.setTimeCreated       (rset.getTimestamp (4));
				if( rtn == null )
				{
					rtn = new ArrayList<DependentCloudModel>();
				}
				rtn.add(dep);

				logger.info(dep.toString());
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
			logger.error("No DependentCloud found");
		}
		else
		{
			logger.error("DependentCloud found = " + rtn.size());
		}
		return rtn;
	}

	public DependentCloudModel insert( DependentCloudModel dependentCloud) throws DAOException
	{
		logger.info("insert() - " + dependentCloud);

		DependentCloudModel rtn  = null;
		Connection          conn = this.getConnection();
		PreparedStatement   stmt = null;
		String              sql  = null;

		try
		{
			sql = "insert into dependent_cloud (guardian_cloudname, dependent_cloudname, payment_id, time_created) values (?, ?, ?, now())";
			logger.info(sql + " : " + dependentCloud);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, dependentCloud.getGuardianCloudName());
			stmt.setString(2, dependentCloud.getDependentCloudName());
			stmt.setString(3, dependentCloud.getPaymentId());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + dependentCloud + " return " + rows + " rows ");
			}
			else
			{
				rtn = dependentCloud;
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
			logger.error("DependentCloud insert failed - " + dependentCloud);
		}
		return rtn;
	}
}
