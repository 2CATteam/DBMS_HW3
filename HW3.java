//Java be like
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class HW3 {
	
	//Class for storing all the options needed to open the DB connection
	public static class DBOptions {
		public String HOSTNAME = "";
		public String DBNAME = "";
		public String USERNAME = "";
		public String PASSWORD = "";
	
		//Constructor takes a filename, which it reads all the proper values from
		public DBOptions(String filename) {
			//Open file
			File src = new File(filename);
			try {
				//Pass to scanner
				Scanner fileRead = new Scanner(src);
				//Read values
				this.HOSTNAME = fileRead.nextLine();
				this.DBNAME = fileRead.nextLine();
				this.USERNAME = fileRead.nextLine();
				this.PASSWORD = fileRead.nextLine();
				//Close scanner
				fileRead.close();
			//If there's an exception, just print it and move on. This isn't Google, we don't need to do all of this.
			//Although, given we need to turn this in, I may change it to some kind of default values...
			} catch (FileNotFoundException e) {
				System.out.println("Credential file not found.\nPlease create a file with your Azure DB credentials.\nBy default, this file should be named src.txt.");
			} catch (Exception e) {
				System.out.println(e);				
			}
		}
	}

    //User input prompt. I copied this part directly from the sample java file and changed it to match this
    final static String PROMPT = 
            "\nPlease select one of the options below: \n" +
            "1) Insert new faculty with department averaging; \n" + 
            "2) Insert new faculty with global averaging; \n" + 
            "3) Print all; \n" +
            "4) Exit";
    
    //Finally, the main function
	public static void main(String[] args) throws SQLException {
		//Create options from an expected local file, src.txt
		final DBOptions cfg = new DBOptions("./src.txt");
		//Format URL based on the above options
		final String URL = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
			cfg.HOSTNAME, cfg.DBNAME, cfg.USERNAME, cfg.PASSWORD);
		
		//Open up basic input
		Scanner in = new Scanner(System.in);
		
		//Fun fact, if you wrap everything in a try-catch block, your code will never crash!
		try {
			//Establish a connection from URL
			final Connection connection = DriverManager.getConnection(URL);
			//Because we run these statements potentially infinite times, it helps a lot to just go ahead and prepare them now.
			final PreparedStatement insert = connection.prepareStatement("INSERT INTO Faculty values (?, ?, ?, ?);");
			final PreparedStatement get = connection.prepareStatement("SELECT * FROM Faculty;");
			
			//Selection loop
			String option = "";
			while (!option.equals("4")) {
				//Show prompt
				System.out.println(PROMPT);
				//Get input
				option = in.next();
				switch (option) {
				//Route selection 1 to option 1
				case "1":
					option1(connection, in, insert);
					break;
				//Route selection 2 to option 2
				case "2":
					option2(connection, in, insert);
					break;
				//Route selection 3 to option 3
				case "3":
					option3(connection, in, get);
					break;
				//The user has quit
				case "4":
					System.out.println("Okay, bye");
					break;
				//Unrecognized option says something and moves on
				default:
					System.out.println("Do better");
				}
			}
		} catch (Exception e) {
			throw e;
		//Can't forget to close that scanner
		} finally {
			in.close();
		}
	}
	
	//Method for option 1, inserting a new faculty with local averaging with math.
	public static void option1(Connection conn, Scanner in, PreparedStatement insert) throws SQLException {		
		//Gets values from user
		System.out.println("Give ID: ");
		final int id = in.nextInt();
		System.out.println("Give name: ");
		final String name = in.next();
		System.out.println("Give department: ");
		final int dId = in.nextInt();
		
		//Asks the DB what the average salary of the department is
		PreparedStatement statement = conn.prepareStatement("SELECT AVG(salary) FROM Faculty WHERE deptid = ?;");
		statement.setInt(1, dId);
		ResultSet result = statement.executeQuery();
		//Read results. If the department does not exist, the average becomes 0.
		result.next();
		final float avg = result.getFloat(1);
		
		//Do the math required to calculate the new salary
		float newSalary = avg;
		if (avg > 50000) {
			newSalary *= .9;
		} else if (avg < 30000) {
		} else {
			newSalary *= .8;
		}
		
		//Set insert parameters
		insert.setInt(1, id);
		insert.setString(2, name);
		insert.setInt(3, dId);
		insert.setFloat(4, newSalary);
		
		//Execute the update and print how many rows were updated
		int updated = insert.executeUpdate();
		System.out.println(String.format("Did thing, %d rows updated", updated));
	}
	
	//Method for option 2, inserting a new faculty with global averaging
	public static void option2(Connection conn, Scanner in, PreparedStatement insert) throws SQLException {
		//Get values from user
		System.out.println("Give ID: ");
		final int id = in.nextInt();
		System.out.println("Give name: ");
		final String name = in.next();
		System.out.println("Give department: ");
		final int dId = in.nextInt();
		System.out.println("Give department to ignore: ");
		final int ignoreDept = in.nextInt();
		
		//Ask the DBMS what the average salary is excluding the indicated department
		PreparedStatement statement = conn.prepareStatement("SELECT AVG(salary) FROM Faculty WHERE deptid != ?;");
		statement.setInt(1, ignoreDept);
		ResultSet result = statement.executeQuery();
		//Read the results
		result.next();
		final float avg = result.getFloat(1);
		
		//Set update parameters
		insert.setInt(1, id);
		insert.setString(2, name);
		insert.setInt(3, dId);
		insert.setFloat(4, avg);
		
		//Execute update and print number of rows affected
		int updated = insert.executeUpdate();
		System.out.println(String.format("Did thing, %d rows updated", updated));
	}

	//Method for option 3, printing all the values
	public static void option3(Connection conn, Scanner in, PreparedStatement statement) throws SQLException {
		//Execute query
		ResultSet resultSet = statement.executeQuery();
		//Display header
		System.out.println("Diplaying faculty stuff: ");
		System.out.println("fid | fname | deptid | salary");
		//For each row, print a formatted string of the results
		while (resultSet.next()) {
			System.out.println(String.format("%d | %s | %d | %.2f ",
                resultSet.getInt(1),
                resultSet.getString(2),
                resultSet.getInt(3),
                resultSet.getFloat(4)));
		}
	}
}
