package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


import connection.DBConnection;



public class Main {

	
	public static void instantiateJDBC() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String get_data_from_commands(String second_command) {
		if (second_command==null) {
			usage();
			return null;
		}
		int start = second_command.lastIndexOf('(');
		int end = second_command.lastIndexOf(')');
		
		if (start == -1 || end == -1) {
			System.err.println("Wrong command!");
			usage();
			return null;
		}
		
		return second_command.substring(start+1, end);
	}
	
	public static void parse_commands(String buffer,DBConnection dbconnection) {
		if (buffer == null) {
			System.err.println("There is nothing to parse! Something is wrong in the commands.");
			return;
		}
		
		// First split by empty space
		String[] subCommands = buffer.split(" ");
		
		if (subCommands.length<2) {
			System.err.println("Wrong command!");
			usage();
			return;
		}
		
		String first_part = subCommands[0];
		// Handle any possible space between ADD|REGISTER FARMER (...)
		String second_part = subCommands.length!=2? subCommands[1]+subCommands[2]:
			subCommands[1];

		if (first_part.equalsIgnoreCase("show")){
			if(second_part.equalsIgnoreCase("tables")) {
				// TODO show tables
				showTables(dbconnection);


			} else {
				System.err.println("Wrong command!");
				usage();
			}
			
		} else if (first_part.equalsIgnoreCase("load")){
			if(second_part.equalsIgnoreCase("data")) {
				// TODO load data
				/*to make it a atomic transaction first I get every data from tables and put them in a insert query string form then added all in
					a big array list which is all queries for csv then execute this array list of strings with add multiple things
				*/
				load_data(dbconnection);

			} else {
				System.err.println("Wrong command!");
				usage();
			}
			
		} else if (first_part.equalsIgnoreCase("query")){
			int query_number = Integer.parseInt(second_part);
			queries(dbconnection,query_number);

		} else if (first_part.equalsIgnoreCase("add")){
			if(second_part.startsWith("FARMERS") || second_part.startsWith("farmers")) {
				String data = second_part;
				String[] tokens = data.split(":");
				ArrayList<String> queries = new ArrayList<>();
				for(int i = 0;i<tokens.length;i++){

					ArrayList<String>farmerstmp = (frQuery(tokens[i]));
					for(int j = 0;j<farmerstmp.size();j++){
						queries.add(farmerstmp.get(j));
					}
				}
				addmultitlething(queries,dbconnection);




			} else if(second_part.startsWith("FARMER") || second_part.startsWith("farmer")) {
				addmultitlething(frQuery(second_part),dbconnection);
			} else if(second_part.startsWith("PRODUCTS") || second_part.startsWith("products")) {
				String data = second_part;
				String[] tokens = data.split(":");
				ArrayList<String> queries = new ArrayList<>();
				for(int i = 0;i<tokens.length;i++){
					queries.add(prQuery(tokens[i]));
				}
				addmultitlething(queries,dbconnection);


			} else if(second_part.startsWith("PRODUCT") || second_part.startsWith("product")) {

				addonething(prQuery(second_part),dbconnection);


			} else if(second_part.startsWith("MARKETS") || second_part.startsWith("markets")) {
				String data = second_part;
				String[] tokens = data.split(":");
				ArrayList<String> queries = new ArrayList<>();
				for(int i = 0;i<tokens.length;i++){
					queries.add(lmQuery(tokens[i]));
				}
				addmultitlething(queries,dbconnection);


			} else if(second_part.startsWith("MARKET") || second_part.startsWith("market")) {
				addonething(lmQuery(second_part),dbconnection);


			} else {
				System.err.println("Wrong command!");
				usage();
			}
			
		} else if (first_part.equalsIgnoreCase("register")){
			if(second_part.startsWith("PRODUCTs") || second_part.startsWith("products")) {
				String data = second_part;
				String[] tokens = data.split(":");
				ArrayList<String> queries = new ArrayList<>();

				for(int i = 0;i<tokens.length;i++){
					queries.add(rgQuery(tokens[i]));
				}
				addmultitlething(queries,dbconnection);


			} else if(second_part.startsWith("PRODUCT") || second_part.startsWith("product")) {
				addonething(rgQuery(second_part),dbconnection);
			} else {
				System.err.println("Wrong command!");
				usage();
			}
		} else {
			System.err.println("Wrong command!");
			usage();
		}
	}
	
	public static void usage() {
		System.out.println("Supported Commands: SHOW TABLES | LOAD DATA | QUERY # | ADD FARMER(...) |"
				+ " ADD PRODUCT(...) | ADD MARKET() | REGISTER PRODUCT(...) | ADD FARMERs(...) |"
				+ " ADD PRODUCTs(...) | ADD MARKETs() | REGISTER PRODUCTs(...)");
	}
	
	public static void main_loop(DBConnection database_connection) {
		
		System.out.println("Command line interface is initiated");
		usage();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String buffer = null;
		try {
			while(true) {
				buffer = reader.readLine();
				if(buffer.equalsIgnoreCase("exit") || buffer.equalsIgnoreCase("quit")){
					// we are done 
					System.out.println("Command line interface is closed.");
					return;
				} else {
					parse_commands(buffer,database_connection);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		String[] burak= new String[4];
		burak[0] = "localhost:3306";
		burak[1] = "202_hw_update";
		burak[2] = "";
		burak[3] = "";


		DBConnection database_connection = null;
		
		instantiateJDBC();

		if(burak.length < 4) {
			System.err.println("Wrong number of arguments!");
			System.err.println("Usage: Main hostname database username password");
		}
		
		String url = String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
				burak[0],burak[1],burak[2],burak[3]);


		try {
			database_connection = new DBConnection(url);
			
			main_loop(database_connection);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(database_connection != null){
				database_connection.close();
			}
		}
		

	}
	// Main show method that uses the other methods
	public static void showTables(DBConnection dbConnection){
		showfarmers(dbConnection);
		showfarmersMail(dbConnection);
		showfarmerPhone(dbConnection);
		showproduct(dbConnection);
		showproduces(dbConnection);
		showbuys(dbConnection);
		showlocalmarket(dbConnection);
		showregister(dbConnection);
	}







	//basic query exuccuter
	public static void addmultitlething(ArrayList<String> queries, DBConnection dbConnection){
		Statement myStmt = null;
		try {
			myStmt =  dbConnection.getConn().createStatement();
			int swichB = 0;
			for(int i = 0;i<queries.size();i++) {
				myStmt.execute(queries.get(i));
			}
			System.out.println("To commit the data enter 1");
			Scanner in = new Scanner(System.in);
			swichB = in.nextInt();
			if(swichB == 1){
				System.out.println("The data has been committed");
				dbConnection.getConn().commit();
			}
			else{
				dbConnection.getConn().rollback();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				dbConnection.getConn().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	public static void addonething(String query, DBConnection dbConnection){
		Statement myStmt;
		try {
			myStmt =  dbConnection.getConn().createStatement();

			int swichB = 0;
			myStmt.execute(query);
			System.out.println("To commit the data enter 1");
			Scanner in = new Scanner(System.in);
			swichB = in.nextInt();
			if( swichB == 1){
				System.out.println("The data has been committed" );
				dbConnection.getConn().commit();
			}
			else{
				dbConnection.getConn().rollback();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				dbConnection.getConn().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	//local market query
	public static String lmQuery(String second_part){
		String data = get_data_from_commands(second_part);
		String[] tokens = data.split(",");
		String query1 = String.format("insert into localmarket " + "(lmname,lmaddress,lmzip,lmcity,lmphone,lmbudget)" +
				"values ('%s','%s',%d,'%s',%d,%d)",tokens[0],tokens[1],Integer.parseInt(tokens[2]), tokens[3],Long.parseLong(tokens[4]),Integer.parseInt(tokens[5]));

		return query1;
	}
	public static String producesQuery(String second_part){
		String data = get_data_from_commands(second_part);
		String[] tokens = data.split(",");
		String query1 = String.format("insert into produces"  + "(fname,flastname,pname,amount,pyear)" + "values('%s','%s','%s',%d,%d)",tokens[0],tokens[1],tokens[2],Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4]));
		return query1;
	}



	//product query
	public static String prQuery(String second_part){
		String data = get_data_from_commands(second_part);
		String[] tokens = data.split(",");
		String query1 = String.format("insert into products" + "(pname,ppdate,phdate,palt,pmintemp,phardness)" +
				"values ('%s','%s','%s',%d,%d,%d)",tokens[0],tokens[1],tokens[2], Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4]),Integer.parseInt(tokens[5]));
		return query1;
	}


	//farmer query
	public static ArrayList<String> frQuery(String second_part){
		String data = get_data_from_commands(second_part);
		String[] tokens = data.split(",");
		String queryf = String.format("insert into farmers" + "(fname,flastname,faddress,fzipcode,fcity)" + "values ('%s','%s','%s',%d,'%s')",tokens[0],tokens[1],tokens[2], Integer.parseInt(tokens[3]),tokens[4]);
		ArrayList<String> queries = new ArrayList<>();
		queries.add(queryf);
		frPQuery(tokens[5],tokens[0],tokens[1],queries);
		frMQuery(tokens[6],tokens[0],tokens[1],queries);

		return queries;

	}
	public static void frMQuery(String mailpart, String name, String lastname, ArrayList<String> queries)  {
			String data = mailpart;
			String[] tokens = data.split(">");
			String query1 = "";
			for(int i = 0;i<tokens.length;i++){
				query1 = String.format("insert into farmersmail" +"(fname,flastname,fmail)" + "values ('%s','%s','%s');",name,lastname,tokens[i]);
				queries.add(query1);
			}

	}
	public static void frPQuery(String phonepart, String name, String lastname, ArrayList<String> queries)  {


			String data = phonepart;
			String[] tokens = data.split(">");
			for(int i = 0;i<tokens.length;i++){
				String query1 = String.format("insert into farmerphone" +"(fname,fsurname, fphonenumber)" + "values ('%s','%s',%d)",name,lastname,Long.parseLong(tokens[i]));
				queries.add(query1);

			}

	}
	public static String buysQuery(String secondpart){
		String data = get_data_from_commands(secondpart);
		String[] tokens = data.split(",");
		String query1 = String.format("insert into buys" + "(fname,flastname,pname,lmname,lmaddress,amount,creditcard)" + "values ('%s','%s','%s','%s','%s',%d,%d)",tokens[0],tokens[1],tokens[2],tokens[3],tokens[4],Integer.parseInt(tokens[5]),Long.parseLong(tokens[6]));
		return query1;
	}





	public static String rgQuery(String second_part) {
		String data = get_data_from_commands(second_part);
		String[] tokens = data.split(",");
		NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
		double d = 0;
		try {
			Number number = format.parse(tokens[4]);
			 d = number.doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String query1 = String.format("insert into register" + "(fname,flastname,pname,amount,price,iban)" + "values ('%s','%s','%s',%d,%s,'%s')",tokens[0],tokens[1],tokens[2],Integer.parseInt(tokens[3]),d,tokens[5]);
		return query1;
	}
	// show farmer tables methods
	public static void showfarmers(DBConnection dbConnection){
		String query1 = "SELECT *FROM farmers";

		System.out.println("Farmer Table");
		System.out.println("name, lastname, address, zipcode, city");
		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String farmer = String.format("%s, %s, %s, %d, %s",rs.getString("fname"),rs.getString("flastname"),rs.getString("faddress"),rs.getInt("fzipcode"),rs.getString("fcity"));
				System.out.println(farmer);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	public static void showfarmersMail(DBConnection dbConnection){
		String query1 = "SELECT *FROM farmersmail";

		System.out.println("Farmer Mail Table");
		System.out.println("name, lastname, mail");
		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String farmerMail = String.format("%s, %s, %s",rs.getString("fname"),rs.getString("flastname"),rs.getString("fmail"));
				System.out.println(farmerMail);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	public static void showfarmerPhone(DBConnection dbConnection){
		String query1 = "SELECT *FROM farmerphone";

		System.out.println("Farmer Phone Table");
		System.out.println("name, lastname, Phonenumber");
		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String farmerPhone = String.format("%s, %s, %d",rs.getString("fname"),rs.getString("fsurname"),rs.getLong("fphonenumber"));
				System.out.println(farmerPhone);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	//show product table method
	public static void showproduct(DBConnection dbConnection){
		String query1 = "SELECT *FROM products";
		System.out.println("Product Table");
		System.out.println("name,Ppdate,Phdate,,Paltitudeleve,PminTemperature,Phardneslevel");

		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String product = String.format("%s ,%s ,%s,%d,%d,%d",rs.getString("pname"),rs.getString("ppdate"),rs.getString("phdate"),rs.getInt("palt"),rs.getInt("pmintemp"),rs.getInt("phardness"));
				System.out.println(product);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	// show produces table method
	public static void showproduces(DBConnection dbConnection){
		String query1 = "SELECT *FROM produces";
		System.out.println("Produces Table");
		System.out.println("fname, flastname, pname, amount, Plantation year");

		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String produces = String.format("%s ,%s ,%s,%d,%d",rs.getString("fname"),rs.getString("flastname"),rs.getString("pname"),rs.getInt("amount"),rs.getInt("pyear"));
				System.out.println(produces);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	// show buys Table method
	public static void showbuys(DBConnection dbConnection){

		String query1 = "SELECT *FROM buys";
		System.out.println("Buy Table");
		System.out.println("fname, flastname, pname, mname, amount, creditcard");


		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String buys = String.format("%s, %s,%s,%s,%s,%d,%d",rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getInt(6),rs.getLong(7));
				System.out.println(buys);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	// show local Market method
	public static void showlocalmarket(DBConnection dbConnection){
		String query1 = "SELECT *FROM localmarket";
		System.out.println("Local Market Table");
		System.out.println("lmname, lmaddress, lmzip, lmcity, lmphone, lmbudget");
		try {
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);
			while (rs.next()){
				String localMarket = String.format("%s, %s, %d, %s, %d, %d",rs.getString("lmname"),rs.getString("lmaddress"),rs.getInt("lmzip"),rs.getString("lmcity"),rs.getLong("lmphone"),rs.getInt("lmbudget"));
				System.out.println(localMarket);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	//show register Table method
	public static void showregister(DBConnection dbConnection){
		String query1 =  "Select *from register";
		System.out.println("Register ");
		System.out.println("fname, flastname, pname, amount, price,iban");
		try{
			Statement st = dbConnection.getConn().createStatement();
			ResultSet rs = st.executeQuery(query1);

			while(rs.next()){
			String register = String.format("%s, %s, %s, %d, %.02f, %s",rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getDouble(5),rs.getString(6));
			System.out.println(register);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	public static ArrayList<String> loadbuys(){
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> buysqueries = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\buys.csv"));
			for(int i = 1;i<lines.size();i++){
				String line = "(" + lines.get(i).replace(";",",") + ")";
				secondpart.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0;i<secondpart.size();i++){
			buysqueries.add(buysQuery(secondpart.get(i)));
		}
		return buysqueries;
	}
	public static ArrayList<String> loadmarkets(){
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> lmqueries = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\markets.csv"));
			for(int i = 1;i<lines.size();i++){
				String line = "(" + lines.get(i).replace(";",",") + ")";
				secondpart.add(line);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0;i<secondpart.size();i++){
			lmqueries.add(lmQuery(secondpart.get(i)));
		}
		return lmqueries;
	}
	public static ArrayList<String> loadfarmers(){
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> frqueries = new ArrayList<>();
		try{
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\farmers.csv"));
			for(int i = 1;i<lines.size();i++){
				String line = "(" + lines.get(i).replace(";",",") + ")";
				secondpart.add(line);
				}
		}catch (IOException e){
			e.printStackTrace();
		}
		for(int i = 0;i<secondpart.size();i++) {
			ArrayList<String> tmp = frQuery(secondpart.get(i));
			for (int j = 0; j < tmp.size(); j++) {
				frqueries.add(tmp.get(j));
			}
		}
		return frqueries;
	}
	public static ArrayList<String> loadproducts() {
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> prqueries = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\products.csv"));
			for (int i = 1; i < lines.size(); i++) {
				String line = "(" + lines.get(i).replace(";", ",") + ")";
				secondpart.add(line);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < secondpart.size(); i++) {
			prqueries.add(prQuery(secondpart.get(i)));
		}
		return prqueries;
	}
	public static ArrayList<String> loadregisters(){
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> rgqueries = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\registers.csv"));
			for (int i = 1; i < lines.size(); i++) {
				String line = "(" + lines.get(i).replace(";",",") + ")";
				secondpart.add(line);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < secondpart.size(); i++) {
			rgqueries.add(rgQuery(secondpart.get(i)));
		}
		return rgqueries;
	}
	public static ArrayList<String> loadproduces(){
		ArrayList<String> secondpart = new ArrayList<>();
		ArrayList<String> prdqueries = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\Burak\\Desktop\\startup_project\\produces.csv"));
			for (int i = 1; i < lines.size(); i++) {
				String line = "(" + lines.get(i).replace(";", ",") + ")";
				secondpart.add(line);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < secondpart.size(); i++) {
			prdqueries.add(producesQuery(secondpart.get(i)));
		}
		return prdqueries;
	}
	public static void queries(DBConnection dbConnection, int query_number){
		switch (query_number){
			case 1:
				System.out.println("query 1 result");
				String query1 = "select fname,flastname,pname from produces P\n  where P.amount = (select max(P1.amount) from produces P1 where P.pname = P1.pname group by P1.pname)";
				System.out.println("product name ----- farmer name ----- farmer last name");
				try {
					Statement st = dbConnection.getConn().createStatement();
					ResultSet rs = st.executeQuery(query1);
					while(rs.next()){
						System.out.println(rs.getString("pname")+"-----"+rs.getString("fname")+"-----"+rs.getString("flastname")+
						"-----");
					}

				} catch (SQLException e){
					e.printStackTrace();
				}
				break;
			case 2:
				String query2 = "select fname,flastname,pname\n" +
						"from buys B\n" +
						"where B.amount = (select max(B1.amount) from buys B1 where B.pname = B1.pname group by B1.pname)";
				try {
					Statement st = dbConnection.getConn().createStatement();
					ResultSet rs  = st.executeQuery(query2);
					while(rs.next()){
						System.out.println(rs.getString("pname")+"-----"+rs.getString("fname") + "-----"+rs.getString("flastname"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				break;
			case 3:
				System.out.println("query 3 result");
				String query3 = "SELECT F.fname , F.flastname FROM farmers F, register R WHERE F.fName=R.fname AND F.flastname=R.flastname AND F.flastname = R.flastname AND R.amount*R.price = (SELECT MAX(R1.Amount*R1.Price) FROM register R1);";
				try{
					Statement st = dbConnection.getConn().createStatement();
					ResultSet rs = st.executeQuery(query3);
					System.out.println("farmer name----farmer last name");
					while(rs.next()){
						System.out.println(rs.getString("fname")+"-----"+rs.getString("flastname"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				System.out.println();
				break;
			case 4:
				System.out.println("query 4 result");
				String query4 = "SELECT M.lmcity, M.lmname FROM localmarket M WHERE M.lmbudget = (SELECT MAX(M1.lmbudget) FROM localmarket M1 WHERE M.lmcity = M1.lmcity Group by M1.lmcity);";
				try{
					Statement st = dbConnection.getConn().createStatement();
					ResultSet rs = st.executeQuery(query4);
					while(rs.next()) {
						System.out.println(rs.getString("lmcity") + "-----" + rs.getString("lmname"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				break;
			case 5:
				System.out.println("query 5 result");
				String query5 = "SELECT (SELECT COUNT(*) FROM farmers)+ (SELECT COUNT(*) from localmarket ) AS SumCount";
				try{
					Statement st = dbConnection.getConn().createStatement();
					ResultSet rs = st.executeQuery(query5);
					while (rs.next()){
						System.out.println(rs.getInt(1));
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
				break;
		}
	}
	public static void load_data(DBConnection dbConnection){
		ArrayList<String> allqueriesforcsv = new ArrayList<>();
		ArrayList<String> marketqueries = loadmarkets();
		ArrayList<String> farmerqueries = loadfarmers();
		ArrayList<String> productqueries = loadproducts();
		ArrayList<String> registerqueries = loadregisters();
		ArrayList<String> buyqueries = loadbuys();
		ArrayList<String> producesqueries = loadproduces();


		for(int i= 0;i<marketqueries.size();i++){
			allqueriesforcsv.add(marketqueries.get(i));
		}


		for(int i = 0;i<farmerqueries.size();i++){
			allqueriesforcsv.add(farmerqueries.get(i));
		}

		for(int i = 0;i<productqueries.size();i++){
			allqueriesforcsv.add(productqueries.get(i));
		}
		for(int i = 0;i<registerqueries.size();i++){
			allqueriesforcsv.add(registerqueries.get(i));
		}

		for(int i = 0;i<buyqueries.size();i++){
			allqueriesforcsv.add(buyqueries.get(i));
		}

		for(int i = 0;i<producesqueries.size();i++){
			allqueriesforcsv.add(producesqueries.get(i));
		}
		addmultitlething(allqueriesforcsv,dbConnection);
	}

}

