package mavenp2versionmatch.db;

import java.io.File;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MySQLDBI {
	private Connection conn;
	//TODO: standardize the dbName and tableName or load them from a config file
	private static final String url = "jdbc:mysql://localhost:3306/eclipse";
	private static final String user = "root";
	private static final String password = "Excellence";
	private static final String dbName = "eclipse";
	private static final String tableName = "maven_p2";
	
	public MySQLDBI () throws SQLException {
		
		//make sure driver is loaded
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("MySQL Driver wasn't loaded");
		}
	}
	
	public void openDB() throws SQLException {
		conn = DriverManager.getConnection(url, user, password);
	}
	
	public void closeDB() throws SQLException {
		if(!conn.isClosed()) {
			conn.close();
		}
	}
	public void addRecord(Map<String, String> colMap) throws SQLException{
		if(conn.isClosed())
			throw new SQLException("Connection is closed, cannot add record");
		
		String colString = "";
		String valString = "";
		Iterator<String> colIter = colMap.keySet().iterator();
		
		//Dynamically create the column string for inserting and
		//add the proper amount of IN variables for the query
		while( colIter.hasNext()) {
			colString += colIter.next();
			valString += "?"; //IN variable for statement
			
			if( colIter.hasNext()) {
				colString += ", ";
				valString += ", ";
			}
		}
		
		String query = "INSERT INTO "+ tableName + "("+colString+")" + "VALUES ("+valString+");";
		
		PreparedStatement stmt = this.conn.prepareStatement(query);
		
		Iterator<String> valueIter = colMap.values().iterator();
		//add the values to the prepared statement
		for( int i = 1; i < colMap.size() + 1; i++ ) {
			stmt.setString( i, valueIter.next());
		}
		
			
		stmt.executeUpdate();
	}

	/**
	 * Updates all records (should only ever be one match however) matching the values 
	 * found in matchMap with the values found in updateMap.
	 * @param matchMap Map of the key-value pairs to match in the database.
	 * @param updateMap Map of the key-value pairs to update in the matching record.
	 * @throws SQLException
	 */
	public void updateRecord(Map<String, String> matchMap, Map<String, String> updateMap) throws SQLException{
		if(conn.isClosed())
			throw new SQLException("Connection is closed, cannot add record");
		
		String matchString = "";
		String updateString = "";
		Iterator<String> matchIter = matchMap.keySet().iterator();
		Iterator<String> updateIter = updateMap.keySet().iterator();

		//Dynamically create the column string for updating and
		//add the proper amount of IN variables for the query
		while( updateIter.hasNext()) {
			updateString += updateIter.next() + "=?";
			if( updateIter.hasNext()) {
				updateString += ", ";
			}
		}
		//System.out.println("update string: " + updateString);
		//Dynamically create the column string for matching and
		//add the proper amount of IN variables for the query
		while( matchIter.hasNext()) {
			matchString += matchIter.next() + "=?";
			if( matchIter.hasNext()) {
				matchString += " AND ";
			}
		}
		//System.out.println("match string: " + matchString);
		
		String query = "UPDATE " + tableName + " SET " + updateString + " WHERE " + matchString + ";";
		
		PreparedStatement stmt = this.conn.prepareStatement(query);
		
		Iterator<String> valueIter1 = updateMap.values().iterator();
		Iterator<String> valueIter2 = matchMap.values().iterator();
		//add the values to the prepared statement
		for( int i = 1; i < updateMap.size() + 1; i++ ) {
			stmt.setString( i, valueIter1.next());
		}
		for( int i = 1; i < matchMap.size() + 1; i++ ) {
			stmt.setString( i + updateMap.size(), valueIter2.next());
		}
		System.out.println("Rows updated: " + stmt.executeUpdate());
		
	}

    public List<MavenP2Version> find(Map<String, String> map) throws SQLException {
		if(conn.isClosed())
			throw new SQLException("Connection is closed, cannot find record");
        
        String where = "";
        String col;
        String[] values = new String[map.size()];
        
        Iterator<String> colIter = map.keySet().iterator();

        for( int i = 0; i < map.size(); i++ ) {
        	col = colIter.next();
        	values[i] = map.get(col);
        	
            where += col + " = ?";
            if( i < map.size() - 1 )
                where += " AND ";
        }
        
        String query = "SELECT * FROM " + tableName + " WHERE " + where;
        
        PreparedStatement stmt = this.conn.prepareStatement(query);
        
        for(int i = 0; i < values.length; i++) {
			stmt.setString( i + 1, values[i]);
        }

        
        ResultSet rs = stmt.executeQuery();
        
		List<MavenP2Version> mpvList = MavenP2Version.convertFromResultSet(rs);
		
		rs.close();
		
		return mpvList;

    }
}