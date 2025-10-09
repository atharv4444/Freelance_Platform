// File: DatabaseManager.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // This will create a file named "freelance_platform.db" in your project's root folder.
    private static final String URL = "jdbc:sqlite:freelance_platform.db";

    public DatabaseManager() {
        createTables();
    }

    // Creates the 'users' table if it doesn't already exist
    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                   + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                   + " name TEXT NOT NULL,"
                   + " email TEXT NOT NULL UNIQUE,"
                   + " type TEXT NOT NULL,"
                   + " skill TEXT,"
                   + " level TEXT,"
                   + " status TEXT NOT NULL"
                   + ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    // Fetches all users from the database
    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT id, name, email, type, skill, level, status FROM users";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new String[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("type"),
                    rs.getString("skill"),
                    rs.getString("level"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    // Adds a new user to the database
    public boolean addUser(String[] userData) {
        String sql = "INSERT INTO users(name, email, type, skill, level, status) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userData[1]); // name
            pstmt.setString(2, userData[2]); // email
            pstmt.setString(3, userData[3]); // type
            pstmt.setString(4, userData[4]); // skill
            pstmt.setString(5, userData[5]); // level
            pstmt.setString(6, userData[6]); // status
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    // Updates a user's status to "Verified"
    public void updateUserStatus(int userId) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Verified");
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
        }
    }

    // Deletes a user from the database by their ID
    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
}
