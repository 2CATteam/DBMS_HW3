import java.io.File;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class HW3 {
	    
	public static class DBOptions {
		public String HOSTNAME = "";
		public String DBNAME = "";
		public String USERNAME = "";
		public String PASSWORD = "";
	}
	
	private static DBOptions getOptions(String filename) {
		DBOptions toReturn = new DBOptions();
		File src = new File(filename);
		try {
			Scanner fileRead = new Scanner(src);
			toReturn.HOSTNAME = fileRead.nextLine();
			toReturn.DBNAME = fileRead.nextLine();
			toReturn.USERNAME = fileRead.nextLine();
			toReturn.PASSWORD = fileRead.nextLine();
			System.out.println(fileRead);
			return toReturn;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

    // User input prompt//
    final static String PROMPT = 
            "\nPlease select one of the options below: \n" +
            "1) Insert new faculty with department averaging; \n" + 
            "2) Insert new faculty with global averaging; \n" + 
            "3) Print all; \n" +
            "4) Exit";
    
	public static void main(String[] args) throws SQLException {
		final DBOptions cfg = getOptions("./src.txt");
		final String URL = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
			cfg.HOSTNAME, cfg.DBNAME, cfg.USERNAME, cfg.PASSWORD);
		Scanner in = new Scanner(System.in);
		try {
			final Connection connection = DriverManager.getConnection(URL);
			final PreparedStatement insert = connection.prepareStatement("INSERT INTO Faculty values (?, ?, ?, ?);");
			final PreparedStatement get = connection.prepareStatement("SELECT * FROM Faculty;");
			
			String option = "";
			while (!option.equals("4")) {
				System.out.println(PROMPT);
				option = in.next();
				switch (option) {
				case "1":
					option1(connection, in, insert);
					break;
				case "2":
					option2(connection, in, insert);
					break;
				case "3":
					option3(connection, in, get);
					break;
				case "4":
					System.out.println("Okay, bye");
					break;
				default:
					System.out.println("Do better");
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			in.close();
		}
	}
	
	public static void option1(Connection conn, Scanner in, PreparedStatement insert) throws SQLException {		
		System.out.println("Give ID: ");
		final int id = in.nextInt();
		System.out.println("Give name: ");
		final String name = in.next();
		System.out.println("Give department: ");
		final int dId = in.nextInt();
		
		PreparedStatement statement = conn.prepareStatement("SELECT AVG(salary) FROM Faculty WHERE deptid = ?;");
		statement.setInt(1, dId);
		ResultSet result = statement.executeQuery();
		result.next();
		final float avg = result.getFloat(1);
		
		float newSalary = avg;
		if (avg > 50000) {
			newSalary *= .9;
		} else if (avg < 30000) {
		} else {
			newSalary *= .8;
		}
		
		insert.setInt(1, id);
		insert.setString(2, name);
		insert.setInt(3, dId);
		insert.setFloat(4, newSalary);
		
		int updated = insert.executeUpdate();
		System.out.println(String.format("Did thing, %d rows updated", updated));
	}
	
	public static void option2(Connection conn, Scanner in, PreparedStatement insert) throws SQLException {
		System.out.println("Give ID: ");
		final int id = in.nextInt();
		System.out.println("Give name: ");
		final String name = in.next();
		System.out.println("Give department: ");
		final int dId = in.nextInt();
		System.out.println("Give department to ignore: ");
		final int ignoreDept = in.nextInt();
		
		PreparedStatement statement = conn.prepareStatement("SELECT AVG(salary) FROM Faculty WHERE deptid != ?;");
		statement.setInt(1, ignoreDept);
		ResultSet result = statement.executeQuery();
		result.next();
		final float avg = result.getFloat(1);
		
		insert.setInt(1, id);
		insert.setString(2, name);
		insert.setInt(3, dId);
		insert.setFloat(4, avg);
		
		int updated = insert.executeUpdate();
		System.out.println(String.format("Did thing, %d rows updated", updated));
	}

	public static void option3(Connection conn, Scanner in, PreparedStatement statement) throws SQLException {
		ResultSet resultSet = statement.executeQuery();
		System.out.println("Diplaying faculty stuff: ");
		System.out.println("fid | fname | deptid | salary");
		while (resultSet.next()) {
			System.out.println(String.format("%d | %s | %d | %.2f ",
                resultSet.getInt(1),
                resultSet.getString(2),
                resultSet.getInt(3),
                resultSet.getFloat(4)));
		}
	}
}
