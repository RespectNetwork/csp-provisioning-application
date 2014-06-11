package net.respectnetwork.csp.application.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.csp.application.dao.CSPProductDAO;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.model.CSPProductModel;

/**
 * @author psharma2
 * 
 */
public class CSPProductDAOImpl extends BaseDAOImpl implements CSPProductDAO {
    // Class Logger
    private static final Logger logger = LoggerFactory
            .getLogger(CSPProductDAOImpl.class);

    public CSPProductDAOImpl() {
        super();
        logger.info("CSPProductDAOImpl() created");
    }

    private CSPProductModel get(ResultSet rset) throws SQLException {
        CSPProductModel cspProduct = new CSPProductModel();

        cspProduct.setProductId(rset.getString(1));
        cspProduct.setProductCost(rset.getBigDecimal(2));
        cspProduct.setProductCurrency(rset.getString(3));
        cspProduct.setProductDuration(rset.getInt(4));
        cspProduct.setCspName(rset.getString(5));
        return cspProduct;
    }

    /**
     * Method to get all the records from CSP_PRODUCT table corresponding to
     * given cspProductId and cspName.
     */
    @Override
    public CSPProductModel get(String cspProductId, String cspName)
            throws DAOException {
        logger.info("get() - " + cspProductId);

        CSPProductModel cspProd = null;
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rset = null;
        String sql = null;

        try {
            sql = "select product_id, product_cost, product_currency, duration_unit_years, csp_name from csp_product where product_id=? and csp_name=? ";
            logger.info(sql + " : " + cspProductId);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cspProductId);
            stmt.setString(2, cspName);

            rset = stmt.executeQuery();
            if (rset.next()) {
                cspProd = this.get(rset);
                logger.info(cspProd.toString());
            }
            rset.close();
            rset = null;
            stmt.close();
            stmt = null;
        } catch (SQLException e) {
            String err = "Failed to execute SQL statement - " + sql;
            logger.error(err, e);
            throw new DAOException(err, e);
        } finally {
            this.closeConnection(conn, stmt, rset);
        }
        if (cspProd == null) {
            logger.error("Active Promotion not found - " + cspProductId);
        }
        return cspProd;
    }
}
