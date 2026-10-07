import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Repair {
    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartspace", "smartspace", "smartspace_dev_pass");
             Statement s = c.createStatement()) {
            System.out.println("Repairing flyway_schema_history...");
            s.execute("DELETE FROM flyway_schema_history WHERE version = '14'");
            s.execute("DROP TABLE IF EXISTS waitlist_entries");
            s.execute("DROP TABLE IF EXISTS pricing_suggestions");
            s.execute("DROP TABLE IF EXISTS society_members");
            System.out.println("Done!");
        }
    }
}
