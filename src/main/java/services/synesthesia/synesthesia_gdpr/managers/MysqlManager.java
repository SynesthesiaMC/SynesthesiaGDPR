package services.synesthesia.synesthesia_gdpr.managers;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import services.synesthesia.synesthesia_gdpr.Main;

public class MysqlManager {

	private Main plugin;
	private Connection conn;

	private String host, port, database, table, user, password;

	public MysqlManager(Main plugin) throws ClassNotFoundException, SQLException {
		this.plugin = plugin;
		if (this.plugin.getConfig().getBoolean("Mysql.Enabled")) {
			this.host = this.plugin.getConfig().getString("Mysql.Host");
			this.port = this.plugin.getConfig().getString("Mysql.Port");
			this.database = this.plugin.getConfig().getString("Mysql.Database");
			this.user = this.plugin.getConfig().getString("Mysql.User");
			this.password = this.plugin.getConfig().getString("Mysql.Password");
			this.table = this.plugin.getConfig().getString("Mysql.TableName").toUpperCase();
		}
	}

	public boolean openConnection() throws SQLException, ClassNotFoundException {

		if (this.plugin.getConfig().getBoolean("Mysql.Enabled")) {

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.err.println("jdbc driver unavailable!");
				return false;
			}

			String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

			try {
				conn = (Connection) DriverManager.getConnection(url, user, password);
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}

			return true;

		} else {
			return false;
		}

	}

	public void closeConnection() throws SQLException {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTable() {

		Statement stmt;
		String query = "CREATE TABLE IF NOT EXISTS " + table + " (UUID VARCHAR(32), ACCEPTED BOOLEAN, PRIMARY KEY ( UUID ));";

		try {
			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void selectionInsert(UUID uuid, boolean accepted) {
		Statement stmt;
		String query = "INSERT INTO " + table + " (UUID, ACCEPTED) VALUES ('" + uuid + "', "
				+ accepted + ");";

		try {
			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void selectionUpdate(UUID uuid, boolean accepted) {
		Statement stmt;
		String query = "UPDATE " + table + " SET ACCEPTED = " + accepted + " WHERE UUID = '" + uuid
				+ "';";

		try {
			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet selectionExtract(UUID uuid) {
		Statement stmt;
		ResultSet result = null;
		try {

			stmt = (Statement) conn.createStatement();
			result = stmt.executeQuery("SELECT * FROM " + table + " WHERE UUID = '" + uuid + "';");
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

	public Connection getConnection() {
		return this.conn;
	}

}
