package gov.iti.jets;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static final String PROPERTIES_FILE = "db.properties";

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        // Use ClassLoader to load the properties file from the classpath
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new SQLException("Unable to find " + PROPERTIES_FILE);
            }


            properties.load(input);


            Class.forName(properties.getProperty("db.driver"));


            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");


            return DriverManager.getConnection(url, username, password);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Failed to load database properties or driver: " + e.getMessage());
        }
    }
}