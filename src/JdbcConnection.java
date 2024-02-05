import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnection {
    private static JdbcConnection instance;
    private Connection conn;

    private JdbcConnection() {
        try {
            // JDBC Driver 등록
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 연결하기
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/sqldb",
                    "root",
                    "admin1111"
            );
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static JdbcConnection getInstance() {
        if (instance == null) {
            instance = new JdbcConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}
