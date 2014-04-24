package net.respectnetwork.csp.application.dao.mysql;

import net.respectnetwork.csp.application.dao.CSPCostOverrideDAO;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.model.CSPCostOverrideModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CSPCostOverrideDAOImpl extends BaseDAOImpl implements CSPCostOverrideDAO {
   private static final Logger logger = LoggerFactory.getLogger(CSPCostOverrideDAOImpl.class);

   public CSPCostOverrideDAOImpl()
   {
      super();
      logger.info("CSPCostOverrideDAOImpl() created");
   }

   private CSPCostOverrideModel get(ResultSet rset) throws SQLException
   {
      CSPCostOverrideModel cspCostOverrideModel = new CSPCostOverrideModel();

      cspCostOverrideModel.setCspCloudName(rset.getString(1));
      cspCostOverrideModel.setPhonePrefix(rset.getString(2));
      cspCostOverrideModel.setCostPerCloudName(rset.getBigDecimal(3));
      cspCostOverrideModel.setCurrency(rset.getString(4));
      cspCostOverrideModel.setMerchantAccountId(rset.getString(5));

      return cspCostOverrideModel;
   }

   @Override
   public CSPCostOverrideModel get(String cspCloudName, String phoneNumber) throws DAOException
   {
      logger.info("get() - " + cspCloudName + ", " + phoneNumber);

      CSPCostOverrideModel rtn = null;
      Connection conn = this.getConnection();
      PreparedStatement stmt = null;
      ResultSet rset = null;
      String sql = null;

      try
      {
         // Query for the longest matching prefix for this phone number
         // This query will need a full scan of all rows with matching csp_cloudname but there
         // will likely only be zero or one matching rows, so it isn't worth optimising
         sql = "select csp_cloudname, phone_prefix, cost_per_cloudname, currency, merchant_account_id from csp_cost_override " +
                 "where csp_cloudname = ? and ? like CONCAT(`phone_prefix`, '%')" +
                 "order by LENGTH(`phone_prefix`) desc limit 1;\n";

         logger.info(sql + " : " + cspCloudName + ", " + phoneNumber);
         stmt = conn.prepareStatement(sql);
         stmt.setString(1, cspCloudName);
         stmt.setString(2, phoneNumber);
         rset = stmt.executeQuery();

         if (rset.next())
         {
            rtn = this.get(rset);
            logger.info(rtn.toString());
         }
         rset.close();
         rset = null;
         stmt.close();
         stmt = null;
      } catch (SQLException e)
      {
         String err = "Failed to execute SQL statement - " + sql;
         logger.error(err, e);
         throw new DAOException(err, e);
      } finally
      {
         this.closeConnection(conn, stmt, rset);
      }
      if (rtn == null)
      {
         logger.error("No cost override found - " + cspCloudName + ", " + phoneNumber);
      }
      return rtn;
   }
}
