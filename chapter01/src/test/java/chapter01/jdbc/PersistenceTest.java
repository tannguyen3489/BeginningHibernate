package chapter01.jdbc;

import chapter01.hibernate.Message;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceTest {
    @BeforeSuite
    public void setup() throws ClassNotFoundException {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DriverManager.getConnection("jdbc:hsqldb:db1;shutdown=true");

            // clear out the old data, if any, so we know the state of the DB
            ps = connection.prepareStatement("DROP TABLE messages IF EXISTS");
            ps.execute();

            ps = connection.prepareStatement(
                    "CREATE TABLE messages ("
                            + "id BIGINT GENERATED BY DEFAULT AS IDENTITY "
                            + "PRIMARY KEY, "
                            + "text VARCHAR(256))"
            );
            ps.execute();

            // eagerly free resources
            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    public void saveMessage() {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DriverManager.getConnection("jdbc:hsqldb:db1;shutdown=true");
            connection.setAutoCommit(false);

            ps = connection.prepareStatement("INSERT INTO messages(text) VALUES (?)");

            ps.setString(1, "Hello, World");
            ps.execute();
            connection.commit();

            // eagerly free resources
            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    @Test(dependsOnMethods = "saveMessage")
    public void readMessage() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Message> list = new ArrayList<>();
        try {
            connection = DriverManager.getConnection("jdbc:hsqldb:db1;shutdown=true");

            ps = connection.prepareStatement("SELECT id, text FROM messages");

            rs = ps.executeQuery();
            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getLong(1));
                message.setText(rs.getString(2));
                list.add(message);
            }

            if (list.size() > 1) {
                Assert.fail("Message configuration in error; table should contain only one."
                        + " Set ddl to drop-create.");
            }
            if (list.size() == 0) {
                Assert.fail("Read of initial message failed; check saveMessage() for errors."
                        + " How did this test run?");
            }
            for (Message m : list) {
                System.out.println(m);
            }
            // eagerly free resources
            rs.close();
            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
