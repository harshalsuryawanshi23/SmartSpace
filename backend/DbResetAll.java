import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbResetAll {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartspace", "smartspace", "smartspace_dev_pass");
            Statement s = c.createStatement();
            
            s.execute("SET FOREIGN_KEY_CHECKS = 0;");
            
            ResultSet rs = s.executeQuery("SHOW TABLES");
            while(rs.next()){
                String table = rs.getString(1);
                System.out.println("Dropping table " + table);
                Statement s2 = c.createStatement();
                s2.execute("DROP TABLE IF EXISTS " + table);
                s2.close();
            }
            
            s.execute("SET FOREIGN_KEY_CHECKS = 1;");
            
            rs.close();
            s.close();
            c.close();
            System.out.println("All tables dropped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
