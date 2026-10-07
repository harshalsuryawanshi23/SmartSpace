import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbReset {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartspace", "smartspace", "smartspace_dev_pass");
            Statement s = c.createStatement();
            
            s.execute("SET FOREIGN_KEY_CHECKS = 0;");
            
            s.execute("DROP TABLE IF EXISTS society_members");
            s.execute("DROP TABLE IF EXISTS societies");
            s.execute("DROP TABLE IF EXISTS halls");
            s.execute("DROP TABLE IF EXISTS society_halls");
            s.execute("DROP TABLE IF EXISTS disputes");
            s.execute("DROP TABLE IF EXISTS dispute");
            s.execute("DROP TABLE IF EXISTS rating");
            s.execute("DROP TABLE IF EXISTS ratings");
            s.execute("DROP TABLE IF EXISTS trust_score");
            s.execute("DROP TABLE IF EXISTS trust_scores");
            s.execute("DROP TABLE IF EXISTS handover_report");
            s.execute("DROP TABLE IF EXISTS handover_reports");
            s.execute("DROP TABLE IF EXISTS users");
            s.execute("DROP TABLE IF EXISTS flyway_schema_history");

            s.execute("SET FOREIGN_KEY_CHECKS = 1;");
            
            s.close();
            c.close();
            System.out.println("Tables dropped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
