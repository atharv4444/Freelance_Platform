import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // This will create a file named "freelance_platform.db" in your project's root folder.
    private static final String URL = "jdbc:sqlite:freelance_platform.db";


    public DatabaseManager() {
        createTables();
    }

    // ========================================================================
    // TABLE CREATION - Creates all required tables
    // ========================================================================
   private void createTables() {
    String[] createTableSQL = {
        // EXISTING: Users table (unchanged)
        "CREATE TABLE IF NOT EXISTS users (" +
        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
        " name TEXT NOT NULL," +
        " email TEXT NOT NULL UNIQUE," +
        " type TEXT NOT NULL," +
        " skill TEXT," +
        " level TEXT," +
        " status TEXT NOT NULL" +
        ")",
        
        // NEW: Projects table
        "CREATE TABLE IF NOT EXISTS projects (" +
        " project_id TEXT PRIMARY KEY," +
        " title TEXT NOT NULL," +
        " description TEXT," +
        " client_name TEXT NOT NULL," +
        " category TEXT NOT NULL," +
        " budget DECIMAL(10,2) NOT NULL," +
        " difficulty TEXT CHECK(difficulty IN ('Beginner', 'Intermediate', 'Expert')) NOT NULL," +
        " deadline_days INTEGER NOT NULL," +
        " status TEXT CHECK(status IN ('Open', 'In Progress', 'Completed', 'Cancelled')) DEFAULT 'Open'," +
        " created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " updated_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " completed_date DATETIME" +
        ")",
        
        // NEW: Bids table
        "CREATE TABLE IF NOT EXISTS bids (" +
        " bid_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
        " freelancer_name TEXT NOT NULL," +
        " amount DECIMAL(10,2) NOT NULL," +
        " completion_days INTEGER NOT NULL," +
        " proposal TEXT," +
        " status TEXT CHECK(status IN ('Pending', 'Accepted', 'Rejected')) DEFAULT 'Pending'," +
        " resume_file_path TEXT," +
        " resume_file_name TEXT," +
        " created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " updated_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE" +
        ")",
        
        // NEW: Project status history
        "CREATE TABLE IF NOT EXISTS project_status_history (" +
        " history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        " project_id TEXT NOT NULL," +
        " old_status TEXT," +
        " new_status TEXT NOT NULL," +
        " changed_by TEXT," +
        " change_reason TEXT," +
        " change_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE" +
        ")"
    };
    
    try (Connection conn = DriverManager.getConnection(URL)) {
        for (String sql : createTableSQL) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
        System.out.println("✅ All database tables created/verified successfully!");
    } catch (SQLException e) {
        System.err.println("❌ Error creating tables: " + e.getMessage());
    }
}

    // ========================================================================
    // USER MANAGEMENT METHODS (EXISTING - Unchanged)
    // ========================================================================
    
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

    // ========================================================================
    // PROJECT MANAGEMENT METHODS (NEW)
    // ========================================================================
    
    // Insert a new project
    public boolean insertProject(ProjectData project) {
    String sql = "INSERT INTO projects (project_id, title, description, client_name, category, budget, difficulty, deadline_days, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
    try (Connection conn = DriverManager.getConnection(URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, project.projectId);
        pstmt.setString(2, project.title);
        pstmt.setString(3, project.description);
        pstmt.setString(4, project.clientName);
        pstmt.setString(5, project.category);
        pstmt.setDouble(6, project.budget);
        pstmt.setString(7, project.difficulty);
        pstmt.setInt(8, project.deadlineDays);
        pstmt.setString(9, project.status);
        
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Project insertion failed: " + e.getMessage());
        return false;
    }
}

    
    // Get all projects
    public ArrayList<ProjectData> getAllProjects() {
        ArrayList<ProjectData> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY created_date DESC";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ProjectData project = new ProjectData();
                project.projectId = rs.getString("project_id");
                project.title = rs.getString("title");
                project.description = rs.getString("description");
                project.clientName = rs.getString("client_name");
                project.category = rs.getString("category");
                project.budget = rs.getDouble("budget");
                project.difficulty = rs.getString("difficulty");
                project.deadlineDays = rs.getInt("deadline_days");
                project.status = rs.getString("status");
                project.createdDate = rs.getTimestamp("created_date");
                project.updatedDate = rs.getTimestamp("updated_date");
                project.completedDate = rs.getTimestamp("completed_date");
                
                projects.add(project);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Project retrieval failed: " + e.getMessage());
        }
        
        return projects;
    }
    
    // Update project status
    public boolean updateProjectStatus(String projectId, String newStatus) {
    // First, get the current status for history
    String currentStatus = getProjectStatus(projectId);
    
    String updateSQL = "UPDATE projects SET status = ?, updated_date = CURRENT_TIMESTAMP WHERE project_id = ?";
    String historySQL = "INSERT INTO project_status_history (project_id, old_status, new_status, changed_by, change_reason) " +
                        "VALUES (?, ?, ?, ?, ?)";
    
    try (Connection conn = DriverManager.getConnection(URL)) {
        conn.setAutoCommit(false);
        
        // Update project status
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            updateStmt.setString(1, newStatus);
            updateStmt.setString(2, projectId);
            updateStmt.executeUpdate();
        }
        
        // Insert status history
        try (PreparedStatement historyStmt = conn.prepareStatement(historySQL)) {
            historyStmt.setString(1, projectId);
            historyStmt.setString(2, currentStatus);
            historyStmt.setString(3, newStatus);
            historyStmt.setString(4, "System");
            historyStmt.setString(5, "Status updated via UI");
            historyStmt.executeUpdate();
        }
        
        conn.commit();
        return true;
        
    } catch (SQLException e) {
        System.err.println("❌ Project status update failed: " + e.getMessage());
        return false;
    }
}

    
    private String getProjectStatus(String projectId) {
        String sql = "SELECT status FROM projects WHERE project_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Status retrieval failed: " + e.getMessage());
        }
        return "Unknown";
    }

    // ========================================================================
    // BID MANAGEMENT METHODS (NEW)
    // ========================================================================
    
    // Insert a new bid
    public boolean insertBid(BidData bid) {
    String sql = "INSERT INTO bids (bid_id, project_id, freelancer_name, amount, completion_days, proposal, status, resume_file_path, resume_file_name) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
    try (Connection conn = DriverManager.getConnection(URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, bid.bidId);
        pstmt.setString(2, bid.projectId);
        pstmt.setString(3, bid.freelancerName);
        pstmt.setDouble(4, bid.amount);
        pstmt.setInt(5, bid.completionDays);
        pstmt.setString(6, bid.proposal);
        pstmt.setString(7, bid.status);
        pstmt.setString(8, bid.resumeFilePath);
        pstmt.setString(9, bid.resumeFileName);
        
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Bid insertion failed: " + e.getMessage());
        return false;
    }
}

    
    // Get all bids
    public ArrayList<BidData> getAllBids() {
        ArrayList<BidData> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids ORDER BY created_date DESC";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                BidData bid = new BidData();
                bid.bidId = rs.getString("bid_id");
                bid.projectId = rs.getString("project_id");
                bid.freelancerName = rs.getString("freelancer_name");
                bid.amount = rs.getDouble("amount");
                bid.completionDays = rs.getInt("completion_days");
                bid.proposal = rs.getString("proposal");
                bid.status = rs.getString("status");
                bid.resumeFilePath = rs.getString("resume_file_path");
                bid.resumeFileName = rs.getString("resume_file_name");
                bid.createdDate = rs.getTimestamp("created_date");
                bid.updatedDate = rs.getTimestamp("updated_date");
                
                bids.add(bid);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Bid retrieval failed: " + e.getMessage());
        }
        
        return bids;
    }
    
    // Get bids by project ID
    public ArrayList<BidData> getBidsByProject(String projectId) {
        ArrayList<BidData> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE project_id = ? ORDER BY created_date DESC";
        
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BidData bid = new BidData();
                bid.bidId = rs.getString("bid_id");
                bid.projectId = rs.getString("project_id");
                bid.freelancerName = rs.getString("freelancer_name");
                bid.amount = rs.getDouble("amount");
                bid.completionDays = rs.getInt("completion_days");
                bid.proposal = rs.getString("proposal");
                bid.status = rs.getString("status");
                bid.resumeFilePath = rs.getString("resume_file_path");
                bid.resumeFileName = rs.getString("resume_file_name");
                bid.createdDate = rs.getTimestamp("created_date");
                bid.updatedDate = rs.getTimestamp("updated_date");
                
                bids.add(bid);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Bid retrieval by project failed: " + e.getMessage());
        }
        
        return bids;
    }
    
    // Update bid status
    public boolean updateBidStatus(String bidId, String newStatus) {
        String sql = "UPDATE bids SET status = ?, updated_date = CURRENT_TIMESTAMP WHERE bid_id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setString(2, bidId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Bid status update failed: " + e.getMessage());
            return false;
        }
    }

    // ========================================================================
    // STATISTICS METHODS (NEW)
    // ========================================================================
    
    public int getTotalProjects() {
        return getCountBySQL("SELECT COUNT(*) FROM projects");
    }
    
    public int getActiveProjects() {
        return getCountBySQL("SELECT COUNT(*) FROM projects WHERE status = 'In Progress'");
    }
    
    public int getCompletedProjects() {
        return getCountBySQL("SELECT COUNT(*) FROM projects WHERE status = 'Completed'");
    }
    
    public int getTotalBids() {
        return getCountBySQL("SELECT COUNT(*) FROM bids");
    }
    
    public int getPendingBids() {
        return getCountBySQL("SELECT COUNT(*) FROM bids WHERE status = 'Pending'");
    }
    
    private int getCountBySQL(String sql) {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Count query failed: " + e.getMessage());
        }
        return 0;
    }
    
    // Utility method to check database connection
    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            return conn != null;
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

// ========================================================================
// DATA CLASSES FOR DATABASE OPERATIONS (NEW)
// ========================================================================

class ProjectData {
    public String projectId;
    public String title;
    public String description;
    public String clientName;
    public String category;
    public double budget;
    public String difficulty;
    public int deadlineDays;
    public String status;
    public Timestamp createdDate;
    public Timestamp updatedDate;
    public Timestamp completedDate;
}

class BidData {
    public String bidId;
    public String projectId;
    public String freelancerName;
    public double amount;
    public int completionDays;
    public String proposal;
    public String status;
    public String resumeFilePath;
    public String resumeFileName;
    public Timestamp createdDate;
    public Timestamp updatedDate;
}
