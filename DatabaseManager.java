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
    // TABLE CREATION - Creates all required tables (FIXED STRING LITERALS)
    // ========================================================================
   private void createTables() {
    String[] createTableSQL = {
        // EXISTING: Users table (FIXED string split)
        "CREATE TABLE IF NOT EXISTS users (" +
        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
        " name TEXT NOT NULL," +
        " email TEXT NOT " + 
        " NULL UNIQUE," +
        " type TEXT NOT NULL," +
        " skill TEXT," +
        " level TEXT," +
        " status TEXT NOT NULL" +
        ")",
        
        // FIXED: Projects table 
        "CREATE TABLE IF NOT EXISTS projects (" +
        
" project_id TEXT PRIMARY KEY," +
        " title TEXT NOT NULL," +
        " description TEXT," +
        " client_name TEXT NOT NULL," + 
        " category TEXT NOT NULL," +
        " budget DECIMAL(10,2) NOT NULL," +
        
" difficulty TEXT CHECK(difficulty IN ('Beginner', 'Intermediate', 'Expert')) NOT NULL," +
       
 " deadline_days INTEGER NOT NULL," +
        " status TEXT CHECK(status IN ('Open', 'In Progress', 'Completed', 'Cancelled')) NOT NULL," +
        " created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " completed_date TIMESTAMP" +
        ")",
       
 
    
    // FIXED: Bids table 
        "CREATE TABLE IF NOT EXISTS bids (" +
        " bid_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
        " freelancer_name TEXT NOT NULL," + 
        " amount DECIMAL(10,2) NOT NULL," +
        " completion_days INTEGER NOT NULL," +
 
        " proposal " +
" TEXT," +
        " status TEXT CHECK(status IN ('Pending', 'Accepted', 'Rejected', 'Withdrawn')) NOT NULL," +
        " resume_file_path TEXT," +
        " resume_file_name TEXT," +
        " created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE" + 
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
        ")",

        // NEW: Milestones 
        "CREATE TABLE IF NOT EXISTS milestones (" +
    
     " milestone_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
     
" description TEXT," +
        " amount DECIMAL(10,2) NOT NULL," +
        " status TEXT CHECK(status IN ('Pending','Funded','Released','Cancelled','Disputed')) DEFAULT 'Pending'," + // ADDED Disputed status
        " payment_method TEXT," +
        " notes TEXT," +
        " created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
   
      " due_date DATETIME," +
        " completed_date DATETIME," +
       
" FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE CASCADE" +
        ")",

        // NEW: Escrow accounts 
        "CREATE TABLE IF NOT EXISTS escrow_accounts (" +
        " escrow_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
     
    " milestone_id TEXT," +
        " client_id INTEGER," +
     
" freelancer_id INTEGER," +
        " amount DECIMAL(10,2) NOT NULL," +
        " status TEXT CHECK(status IN ('Funded','Partially Released','Released','Refunded','On Hold')) DEFAULT 'Funded'," +
        " created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE CASCADE," +
        " FOREIGN KEY(milestone_id) REFERENCES " + 
        " milestones(milestone_id) ON DELETE SET NULL" 
+ 
        ")",

        // NEW: Invoices 
        "CREATE TABLE IF NOT EXISTS invoices (" +
        " invoice_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
  
      " client_id INTEGER," +
        " freelancer_id INTEGER," +
        " amount DECIMAL(10,2) " +
" NOT NULL," +
        " status TEXT CHECK(status IN ('Draft','Sent','Paid','Overdue','Cancelled')) DEFAULT 'Draft'," +
        " description TEXT," +
        " created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " due_date DATETIME," +
        " FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE CASCADE" +
        ")",

        // NEW: Disputes 
        "CREATE TABLE " +
"IF NOT EXISTS disputes (" +
        " dispute_id TEXT PRIMARY KEY," +
        " project_id TEXT NOT NULL," +
       
 " milestone_id TEXT," +
        " raised_by TEXT CHECK(raised_by IN ('Client','Freelancer','Admin')) NOT NULL," +
        " reason TEXT," +
        " status TEXT CHECK(status IN ('Open','Under Review','Resolved','Escalated','Closed')) DEFAULT 'Open'," +
        " resolution TEXT," +
   
" created_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " updated_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
        " FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE " + 
        " CASCADE," + 
        " FOREIGN KEY(milestone_id) REFERENCES milestones(milestone_id) ON DELETE SET NULL" +
        ")",

        // Helpful indexes
        "CREATE INDEX IF NOT EXISTS idx_milestones_project ON milestones(project_id)",
        "CREATE INDEX IF NOT EXISTS idx_escrow_project ON escrow_accounts(project_id)",
        "CREATE INDEX IF NOT EXISTS idx_invoices_project ON invoices(project_id)",
        "CREATE INDEX IF NOT EXISTS idx_disputes_project ON disputes(project_id)"

    
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
            pstmt.setString(1, userData[1]);
// name
            pstmt.setString(2, userData[2]);
// email
            pstmt.setString(3, userData[3]);
// type
            pstmt.setString(4, userData[4]);
// skill
            pstmt.setString(5, userData[5]);
// level
            pstmt.setString(6, userData[6]);
// status
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
    // PROJECT MANAGEMENT METHODS (NEW - Unchanged)
    // ========================================================================
    
    // Insert a new project (FIXED: Uses client_name)
    public boolean insertProject(ProjectData project) {
    String sql = "INSERT INTO projects (project_id, title, description, client_name, category, budget, difficulty, deadline_days, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, project.projectId);
        pstmt.setString(2, project.title);
        pstmt.setString(3, project.description);
        pstmt.setString(4, project.clientName); // FIXED: Use clientName
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

    
    // Get all projects (FIXED: Uses client_name)
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
// FIXED: Use clientName
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
    
    // Update project status (unchanged)
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
    // BID MANAGEMENT METHODS (NEW - Unchanged)
    // ========================================================================
    
    // Insert a new bid (FIXED: Uses freelancer_name)
    public boolean insertBid(BidData bid) {
    String sql = "INSERT INTO bids (bid_id, project_id, freelancer_name, amount, completion_days, proposal, status, resume_file_path, resume_file_name) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, bid.bidId);
        pstmt.setString(2, bid.projectId);
        pstmt.setString(3, bid.freelancerName); // FIXED: Use freelancerName
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

    
    // Get all bids (FIXED: Uses freelancer_name)
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
                bid.freelancerName = rs.getString("freelancer_name"); // FIXED: Use freelancerName
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
    
    // Get bids by project ID (FIXED: Uses freelancer_name)
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
                bid.freelancerName = rs.getString("freelancer_name"); // FIXED: Use freelancerName
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
    
    // Update bid status (unchanged)
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
    // PAYMENT MANAGEMENT METHODS (NEW & MODIFIED)
    // ========================================================================

    // ===== Milestones (Unchanged) =====
    public int insertMilestone(String milestoneId, String projectId, String description, double amount,
                            String paymentMethod, String notes) {
        String sql = "INSERT INTO milestones (milestone_id, project_id, description, amount, payment_method, notes) " +
                    "VALUES " + 
" (?, ?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, milestoneId);
            ps.setString(2, projectId);
            ps.setString(3, description);
            ps.setDouble(4, amount);
            ps.setString(5, paymentMethod);
            ps.setString(6, notes);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public List<Object[]> getMilestonesByProject(String projectId) {
        String sql = "SELECT milestone_id, project_id, description, amount, status, payment_method, notes, created_date, due_date, completed_date " +
                    "FROM milestones WHERE project_id = ? OR ? = '' ORDER BY created_date DESC"; // Added OR '' for fetching all
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("milestone_id"),
                        rs.getString("project_id"),
        
                 rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("status"),
                        rs.getString("payment_method"),
            
             rs.getString("notes"),
                        rs.getString("created_date"),
                        rs.getString("due_date"),
                        rs.getString("completed_date")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return rows;
    }

    public int updateMilestoneStatus(String milestoneId, String newStatus) {
        String sql = "UPDATE milestones SET status = ?, completed_date = CASE WHEN ? IN ('Released','Cancelled') THEN CURRENT_TIMESTAMP ELSE completed_date END " +
                    "WHERE milestone_id = ?";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setString(3, milestoneId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    // ===== Escrow (MODIFIED) =====
    public int insertEscrow(String escrowId, String projectId, String milestoneId, Integer clientId, Integer freelancerId, double amount) {
        String sql = "INSERT INTO escrow_accounts (escrow_id, project_id, milestone_id, client_id, freelancer_id, amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, escrowId);
            ps.setString(2, projectId);
            ps.setString(3, milestoneId);
            if (clientId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, clientId);
            if (freelancerId == null) ps.setNull(5, java.sql.Types.INTEGER);
            else ps.setInt(5, freelancerId);
            ps.setDouble(6, amount);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }
    
    // NEW: Get Escrow by Project
    public List<Object[]> getEscrowByProject(String projectId) {
        String sql = "SELECT escrow_id, project_id, milestone_id, client_id, freelancer_id, amount, status, created_date " +
                    "FROM escrow_accounts WHERE project_id = ? OR ? = '' ORDER BY created_date DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("escrow_id"),
                        rs.getString("project_id"),
                        rs.getString("milestone_id"),
                        rs.getInt("client_id"),
                        rs.getInt("freelancer_id"),
                        rs.getDouble("amount"),
                        rs.getString("status"),
                        rs.getString("created_date")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return rows;
    }

    public int updateEscrowStatus(String escrowId, String status) {
        String sql = "UPDATE escrow_accounts SET status = ? WHERE escrow_id = ?";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, escrowId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }
    
    // NEW: Update Escrow Status by Milestone ID (for dispute/release)
    public int updateEscrowStatusByMilestone(String milestoneId, String status) {
        String sql = "UPDATE escrow_accounts SET status = ? WHERE milestone_id = ?";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, milestoneId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public double getEscrowTotalByProject(String projectId) {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM escrow_accounts WHERE project_id = ? OR ? = '' AND status IN ('Funded','On Hold')";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return 0.0;
    }

    // ===== Invoices (MODIFIED) =====
    public int insertInvoice(String invoiceId, String projectId, Integer clientId, Integer freelancerId,
                            double amount, String description, String dueDateIso) {
        String sql = "INSERT INTO invoices (invoice_id, project_id, client_id, freelancer_id, amount, description, due_date) " +
                    "VALUES (?, ?, ?, " + 
" ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, invoiceId);
            ps.setString(2, projectId);
            if (clientId == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, clientId);
            if (freelancerId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, freelancerId);
            ps.setDouble(5, amount);
            ps.setString(6, description);
            ps.setString(7, dueDateIso);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public int updateInvoiceStatus(String invoiceId, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, invoiceId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public List<Object[]> getInvoicesByProject(String projectId) {
        String sql = "SELECT invoice_id, project_id, client_id, freelancer_id, amount, status, description, created_date, due_date " +
                    "FROM invoices WHERE project_id = ? OR ? = '' ORDER BY created_date DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("invoice_id"),
                        rs.getString("project_id"),
        
                 rs.getObject("client_id"),
                        rs.getObject("freelancer_id"),
                        rs.getDouble("amount"),
                        rs.getString("status"),
            
             rs.getString("description"),
                        rs.getString("created_date"),
                        rs.getString("due_date")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return rows;
    }

    // ===== Disputes (MODIFIED) =====
    public int insertDispute(String disputeId, String projectId, String milestoneId, String raisedBy, String reason) {
        String sql = "INSERT INTO disputes (dispute_id, project_id, milestone_id, raised_by, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, disputeId);
            ps.setString(2, projectId);
            ps.setString(3, milestoneId);
            ps.setString(4, raisedBy);
            ps.setString(5, reason);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public int updateDispute(String disputeId, String status, String resolution) {
        String sql = "UPDATE disputes SET status = ?, resolution = ?, updated_date = CURRENT_TIMESTAMP WHERE dispute_id = ?";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            // Use setString for resolution, which handles null properly for SQLite
            if (resolution == null) ps.setNull(2, java.sql.Types.VARCHAR); else ps.setString(2, resolution); 
            ps.setString(3, disputeId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0;
        }
    }

    public List<Object[]> getDisputesByProject(String projectId) {
        String sql = "SELECT dispute_id, project_id, milestone_id, raised_by, reason, status, resolution, created_date, updated_date " +
                    "FROM disputes WHERE project_id = ? OR ? = '' ORDER BY created_date DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("dispute_id"),
                        rs.getString("project_id"),
        
                 rs.getString("milestone_id"),
                        rs.getString("raised_by"),
                        rs.getString("reason"),
                        rs.getString("status"),
           
             rs.getString("resolution"),
                        rs.getString("created_date"),
                        rs.getString("updated_date")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return rows;
    }

    // ===== Dashboard helpers (Unchanged) =====
    public int getMilestoneCountByProject(String projectId) {
        String sql = "SELECT COUNT(*) AS c FROM milestones WHERE project_id = ? OR ? = ''";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("c");
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return 0;
    }

    public int getOpenDisputeCountByProject(String projectId) {
        String sql = "SELECT COUNT(*) AS c FROM disputes WHERE (project_id = ? OR ? = '') AND status IN ('Open','Under Review','Escalated')";
        try (Connection c = DriverManager.getConnection(URL);
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("c");
            }
        } catch (SQLException e) { e.printStackTrace();
        }
        return 0;
    }


    // ========================================================================
    // STATISTICS METHODS (NEW - Unchanged)
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
                return 
rs.getInt(1);
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
// DATA CLASSES FOR DATABASE OPERATIONS (NEW - Unchanged)
// ========================================================================

class ProjectData {
    public String projectId;
    public String title;
    public String description;
    public String clientName; 
    public int clientId;
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
    public int freelancerId;
    public double amount;
    public int completionDays;
    public String proposal;
    public String status;
    public String resumeFilePath;
    public String resumeFileName;
    public Timestamp createdDate;
    public Timestamp updatedDate;
}