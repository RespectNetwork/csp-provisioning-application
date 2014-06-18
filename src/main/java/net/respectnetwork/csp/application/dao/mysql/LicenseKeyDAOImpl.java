package net.respectnetwork.csp.application.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.respectnetwork.csp.application.constants.Status;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.LicenseKeyDAO;
import net.respectnetwork.csp.application.model.LicenseKeyModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseKeyDAOImpl extends BaseDAOImpl implements LicenseKeyDAO {
    private static final Logger logger = LoggerFactory
            .getLogger(LicenseKeyDAOImpl.class);

    public LicenseKeyDAOImpl() {
        super();
        logger.info("LicenceKeyDAOImpl() created");
    }

    private LicenseKeyModel get(ResultSet rset) throws SQLException {
        LicenseKeyModel licenceKeyModel = new LicenseKeyModel();

        licenceKeyModel.setCspCloudNumber(rset.getString(1));
        licenceKeyModel.setUserCloudNumber(rset.getString(2));
        licenceKeyModel.setKeyName(rset.getString(3));
        licenceKeyModel.setKeyValue(rset.getString(4));
        return licenceKeyModel;
    }

    public LicenseKeyModel get(String cspCloudNumber, String userCloudNumber)
            throws DAOException {
        logger.info("Fetch licence key for : " + cspCloudNumber);
        LicenseKeyModel licenceKeyModel = null;
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rset = null;
        String sql = null;
        try {
            sql = "select csp_cloudnumber, user_cloudnumber, key_name, key_value from license_key where csp_cloudnumber = ? and user_cloudnumber = ?";
            logger.info(sql + " : " + cspCloudNumber);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cspCloudNumber);
            stmt.setString(2, userCloudNumber);
            rset = stmt.executeQuery();
            if (rset.next()) {
                licenceKeyModel = this.get(rset);
            }
        } catch (SQLException e) {
            String err = "Failed to execute SQL statement - " + sql;
            logger.error(err, e);
            throw new DAOException(err, e);
        } finally {
            this.closeConnection(conn, stmt, rset);
        }
        if (licenceKeyModel == null) {
            logger.error("No License key found");
        } else {
            // logger.error("DependentCloud found = " + licenceKeyModel.size());
        }
        return licenceKeyModel;
    }

    public Status insert(LicenseKeyModel licenceKeyModel) throws DAOException {
        logger.info("insert() - " + licenceKeyModel);

        LicenseKeyModel rtn = null;
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        String sql = null;

        try {
            sql = "insert into license_key (csp_cloudnumber, user_cloudnumber, key_name, key_value) values (?, ?, ?, ?)";
            logger.info(sql + " : " + licenceKeyModel);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, licenceKeyModel.getCspCloudNumber());
            stmt.setString(2, licenceKeyModel.getUserCloudNumber());
            stmt.setString(3, licenceKeyModel.getKeyName());
            stmt.setString(4, licenceKeyModel.getKeyValue());

            int rows = stmt.executeUpdate();
            if (rows != 1) {
                logger.error(sql + " : " + licenceKeyModel + " return " + rows
                        + " rows ");
            } else {
                rtn = licenceKeyModel;
            }
            stmt.close();
            stmt = null;
        } catch (SQLException e) {
            String err = "Failed to execute SQL statement - " + sql;
            logger.error(err, e);
            throw new DAOException(err, e);
        } finally {
            this.closeConnection(conn, stmt);
        }
        if (rtn == null) {
            logger.error("License key insert failed - " + licenceKeyModel);
        }
        return Status.SUCCESS;
    }
}
