package com.emls;

import java.sql.*;

public class AuthService {
    public Session login(String username, char[] password) throws SQLException {
        String sql = """
            SELECT USER_ID, USERNAME, PASSWORD_HASH, ROLE, CUSTOMER_ID, ACTIVE
            FROM USER_ACCOUNT WHERE LOWER(USERNAME) = LOWER(?)
            """;
        try (Connection c = Config.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"Y".equals(rs.getString("ACTIVE"))
                        || !PasswordUtil.verify(password, rs.getString("PASSWORD_HASH"))) {
                    return null;
                }
                long customerId = rs.getLong("CUSTOMER_ID");
                Long cid = rs.wasNull() ? null : customerId;
                return new Session(rs.getLong("USER_ID"), rs.getString("USERNAME"),
                        rs.getString("ROLE"), cid);
            }
        }
    }

    public void createUser(String username, char[] password, String role, Long customerId) throws SQLException {
        String sql = "INSERT INTO USER_ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, ROLE, CUSTOMER_ID, ACTIVE) "
                   + "VALUES (USER_ACCOUNT_SEQ.NEXTVAL, ?, ?, ?, ?, 'Y')";
        try (Connection c = Config.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, role);
            if (customerId == null) ps.setNull(4, Types.NUMERIC); else ps.setLong(4, customerId);
            ps.executeUpdate();
        }
    }
    
public void createClientAccount(
        String customerName,
        String email,
        String phone,
        String address,
        String username,
        char[] password
) throws SQLException {

    String customerSql =
        "INSERT INTO CUSTOMER " +
        "(CUSTOMER_ID, CUSTOMER_NAME, EMAIL, PHONE, ADDRESS) " +
        "VALUES (CUSTOMER_SEQ.NEXTVAL, ?, ?, ?, ?)";

    String accountSql =
        "INSERT INTO USER_ACCOUNT " +
        "(USER_ID, USERNAME, PASSWORD_HASH, ROLE, CUSTOMER_ID, ACTIVE) " +
        "VALUES (USER_ACCOUNT_SEQ.NEXTVAL, ?, ?, 'CLIENT', ?, 'Y')";

    try (Connection c = Config.connect()) {
        c.setAutoCommit(false);

        try {
            long customerId;

            try (PreparedStatement ps = c.prepareStatement(
                    customerSql,
                    new String[]{"CUSTOMER_ID"})) {

                ps.setString(1, customerName.trim());
                ps.setString(2, email.trim());
                ps.setString(3, phone == null || phone.isBlank()
                        ? null : phone.trim());
                ps.setString(4, address == null || address.isBlank()
                        ? null : address.trim());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Could not retrieve customer ID.");
                    }
                    customerId = rs.getLong(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(accountSql)) {
                ps.setString(1, username.trim());
                ps.setString(2, PasswordUtil.hash(password));
                ps.setLong(3, customerId);
                ps.executeUpdate();
            }

            c.commit();

        } catch (Exception ex) {
            c.rollback();

            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            }

            throw new SQLException("Could not create client account.", ex);
        }
    }
}

}
