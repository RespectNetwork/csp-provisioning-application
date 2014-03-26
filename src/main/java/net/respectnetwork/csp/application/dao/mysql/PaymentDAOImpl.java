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

import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.dao.PaymentDAO;
import net.respectnetwork.csp.application.dao.DAOException;

public class PaymentDAOImpl extends BaseDAOImpl implements PaymentDAO
{
	private static final Logger logger = LoggerFactory.getLogger(PaymentDAOImpl.class);

	public PaymentDAOImpl()
	{
		super();
		logger.info("PaymentDAOImpl() created");
	}

	private PaymentModel get( ResultSet rset ) throws SQLException
	{
		PaymentModel pay = new PaymentModel();

		pay.setPaymentId          (rset.getString    (1));
		pay.setCspCloudName       (rset.getString    (2));
		pay.setPaymentReferenceId (rset.getString    (3));
		pay.setPaymentResponseCode(rset.getString    (4));
		pay.setAmount             (rset.getBigDecimal(5));
		pay.setCurrency           (rset.getString    (6));
		pay.setTimeCreated        (rset.getTimestamp (7));

		return pay;
	}

	public PaymentModel get( String paymentId ) throws DAOException
	{
		logger.info("get() " + paymentId);

		PaymentModel      rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		ResultSet         rset = null;
		String            sql  = null;

		try
		{
			sql = "select payment_id, csp_cloudname, payment_reference_id, payment_response_code, amount, currency, time_created from payment where payment_id = ?";
			logger.info(sql + " : " + paymentId);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, paymentId);
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
			logger.error("Payment not found - " + paymentId);
		}
		return rtn;
	}

	public PaymentModel insert( PaymentModel payment ) throws DAOException
	{
		logger.info("insert() - " + payment);

		PaymentModel      rtn  = null;
		Connection        conn = this.getConnection();
		PreparedStatement stmt = null;
		String            sql  = null;

		try
		{
			sql = "insert into payment (payment_id, csp_cloudname, payment_reference_id, payment_response_code, amount, currency, time_created) values (?, ?, ?, ?, ?, ?, now())";
			logger.info(sql + " : " + payment);
			stmt = conn.prepareStatement(sql);
			stmt.setString    (1, payment.getPaymentId());
			stmt.setString    (2, payment.getCspCloudName());
			stmt.setString    (3, payment.getPaymentReferenceId());
			stmt.setString    (4, payment.getPaymentResponseCode());
			stmt.setBigDecimal(5, payment.getAmount());
			stmt.setString    (6, payment.getCurrency());

			int rows = stmt.executeUpdate();
			if( rows != 1 )
			{
				logger.error(sql + " : " + payment + " return " + rows + " rows ");
			}
			else
			{
				rtn = payment;
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
			logger.error("Payment insert failed - " + payment);
		}
		return rtn;
	}
}
