package org.ecs160.a2;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Data {

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:src/org/ecs160/a2/db/taskApp.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    public void createTable() {
        Connection c = null;
        Statement stmt = null;

        try {

            Class.forName("org.sqlite.JDBC");

            c = this.connect();
            System.out.println("Database Opened...\n");
            stmt = c.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS tasks " +
            "(name TEXT PRIMARY KEY NOT NULL," +
            " size TEXT NOT NULL, " +
            " runTime INTEGER, " +
            " startTime TEXT," +
            " description TEXT)";

            stmt.executeUpdate(sql);
            stmt.close();
            c.close();

        }

        catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }
    
    public void insert(String name, String size) {
        String sql = "INSERT INTO tasks(name,size,runTime) VALUES(?,?,?) ";
        try (Connection conn = this.connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, size);
            pstmt.setInt(3, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet GetTask(String taskName) {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT DISTINCT * " +
                "FROM tasks " +
                "WHERE name = \""+taskName+"\"";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public boolean TaskExists(String taskName) {
        String sql = "SELECT Count(*) " +
                "FROM tasks " +
                "WHERE name = ?";
        ResultSet rs = null;
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taskName);
            rs = pstmt.executeQuery();
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public String GetMinRuntimeTask(String size) {
        String shortestTask = null;
        String sql = "SELECT DISTINCT * " +
                "FROM tasks" +
                (size.isEmpty() ? "" : " WHERE size = \"" + size + "\"") +
                " ORDER BY runTime";
        ResultSet rs = null;
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            rs = pstmt.executeQuery();
            shortestTask = rs.getString("name");
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return shortestTask;
    }

    public String GetMaxRuntimeTask(String size) {
        String longestTask = null;
        String sql = "SELECT DISTINCT * " +
                "FROM tasks" +
                (size.isEmpty() ? "" : " WHERE size = \"" + size + "\"") +
                " ORDER BY runTime DESC";
        ResultSet rs = null;
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            rs = pstmt.executeQuery();
            longestTask = rs.getString("name");
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return longestTask;
    }

    public ResultSet Get3LongestTasks(String size) {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT DISTINCT name " +
                "FROM tasks " +
                (size.isEmpty() ? "" : " WHERE size = \"" + size + "\"") +
                " ORDER BY runTime DESC" +
                " LIMIT 3";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public ResultSet Get3NewestTasks(String size) {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT DISTINCT name " +
                "FROM tasks " +
                (size.isEmpty() ? "" : " WHERE size = \"" + size + "\"") +
                " ORDER BY _rowid_ DESC" +
                " LIMIT 3";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public ResultSet GetAllTasks() {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * " +
                "FROM tasks " +
                "ORDER BY _rowid_ DESC ";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public ResultSet GetSearchResultsByName(String searchInput) {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * " +
            " FROM tasks " +
            " WHERE name LIKE '%"+searchInput+"%'" +
            " ORDER BY LENGTH(name)";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public ResultSet GetSearchResultsBySize(String searchInput) {
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * " +
                "FROM tasks " +
                " WHERE size = \""+searchInput+"\" "+
                " ORDER BY _rowid_ DESC ";
        try {
            c = this.connect();
            stmt = c.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public int GetSizeClassSize(String searchInput) {
        int sizeClassCount = 0;
        String sql = "SELECT Count(*) " +
                "FROM tasks " +
                "WHERE size = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchInput);
            ResultSet rs = pstmt.executeQuery();
            sizeClassCount = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sizeClassCount;
    }

    public int CountAllTasks() {
        int count = 0;
        String sql = "SELECT Count(*) " +
                "FROM tasks";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            count = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return count;
    }

    public boolean isGivenTaskRunning(String taskName) {
        boolean isGivenTaskRunning = isAnyTaskRunning();
        try {
            ResultSet rs = GetTask(taskName);
            isGivenTaskRunning = !rs.getString("startTime").isEmpty();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isGivenTaskRunning;
    }

    public boolean isAnyTaskRunning() {
        return !runningTask.isEmpty();
    }

    public void RenameTask(String oldTaskName, String newTaskName) {
        UpdateTextColumnOfTask(oldTaskName, "name", newTaskName);
    }

    public void ResizeTask(String taskName, String newSize) {
        UpdateTextColumnOfTask(taskName, "size", newSize);
    }

    public void SetTaskDescription(String taskName, String description) {
        UpdateTextColumnOfTask(taskName, "description", description);
    }

    public void StartTask(String taskName) {
        LocalDateTime startTimeLDT = LocalDateTime.now();
        DateTimeFormatter dtf = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss"); 

        UpdateTextColumnOfTask(taskName, "startTime", dtf.format(startTimeLDT));
        runningTask = taskName;
    }

    public void StartTask(String taskName, String startTime) {
        UpdateTextColumnOfTask(taskName, "startTime", startTime);
        runningTask = taskName;
    }

    public LocalDateTime GetStartTimeObject(String taskName, DateTimeFormatter dtf) {
        ResultSet rs = null;

        LocalDateTime startTimeLDT = LocalDateTime.now();
        String sql = "SELECT startTime " +
            "FROM tasks " +
            "WHERE name = ?";

        try (Connection conn = this.connect(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taskName);
            rs = pstmt.executeQuery();
            startTimeLDT = LocalDateTime.parse(rs.getString("startTime"),dtf);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return startTimeLDT;
    }

    public void IncrementRuntime(String taskName, int timeDifference) {
        String sql = "UPDATE tasks "+
                "SET runTime = runTime + ? " +
                "WHERE name = ?";
        
        try (Connection conn = this.connect(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, timeDifference);
            pstmt.setString(2, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void DeleteStartTime(String taskName) {
        String sql = "UPDATE tasks SET startTime = NULL " +
         "WHERE name = ?";
        
        try (Connection conn = this.connect(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        runningTask = "";
    }

    public void UpdateTextColumnOfTask(String taskName, String column, String newVal) {
        String sql = "UPDATE tasks " +
                "SET "+column+" = ? " +
                "WHERE name = ?";

        try (Connection conn = this.connect(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newVal);
            pstmt.setString(2, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private String runningTask;
        
}
