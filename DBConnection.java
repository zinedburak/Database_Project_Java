package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	
	private Connection conn = null;

	
	
	public DBConnection(String url) throws SQLException {
			conn = DriverManager.getConnection(url);
			conn.setAutoCommit(false);
	}
	public Connection getConn(){
	    return conn;
    }
	
	public void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * A method to send select statement to the underlying DBMS (e.g., "select * from Table1")
	 * @param query_statement A query to run on the underlying DBMS 
	 * @return Resultset the query result. 
	 */
	public ResultSet send_query(String query_statement) {
		// Feel free to make them Class attributes
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query_statement);

		}
		catch (SQLException ex){
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		finally {
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) {
				System.out.println(rs);
		        try {
		            rs.close();
		        } catch (SQLException sqlEx) { } // ignore

		        rs = null;
		    }

		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException sqlEx) { } // ignore

		        stmt = null;
		    }
		}	
	    return rs;
		
	}
	
	// TODO Write a method to insert new data into the DBMS

}
