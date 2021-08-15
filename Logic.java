package org.ecs160.a2;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Logic {

    public Logic() {
        sqler = new Data();
        sqler.createTable();
    }

    public void CreateNewTask(String taskName, String size) {
        sqler.insert(taskName, size);
    }

    public ResultSet GetTask(String taskName) {
        return sqler.GetTask(taskName);
    }

    public boolean TaskExists(String taskName) {
        return sqler.TaskExists(taskName);
    }
    
    public ResultSet GetAllTasks() {
        return sqler.GetAllTasks();
    }

    public HashMap<String, String> GetMinandMaxRuntimeTasks(String size) {
        HashMap<String, String> maxAndMinTasks = new HashMap<>();

        maxAndMinTasks.put("shortestTask", sqler.GetMinRuntimeTask(size));
        maxAndMinTasks.put("longestTask",sqler.GetMaxRuntimeTask(size));

        return maxAndMinTasks;
    }

    public ResultSet GetSearchResultsBySize(String size) {
        return sqler.GetSearchResultsBySize(size);
    }

    public ArrayList<String> Get3LongestTasks(String size) {
        ArrayList<String> ThreeLongestTasks = new ArrayList<>();
        try {
            ResultSet rs = sqler.Get3LongestTasks(size);
            while (rs.next()) {
                String name = rs.getString("name");
                System.out.println(name);
                ThreeLongestTasks.add(name);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ThreeLongestTasks;
    }

    public ArrayList<String> Get3NewestTasks(String size) {
        ArrayList<String> ThreeNewestTasks = new ArrayList<>();
        try {
            ResultSet rs = sqler.Get3NewestTasks(size);
            while (rs.next()) {
                String name = rs.getString("name");
                ThreeNewestTasks.add(name);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ThreeNewestTasks;
    }

    public HashMap<String, Integer> SizeSummaryStatistics(String size) {
        if(size.isEmpty()) return FullSummaryStatistics();

        HashMap<String, Integer> stats = new HashMap<>();
        int numTasks = CountSizeClass(size);
        //if(numTasks < 2) return stats;
        if(numTasks == 0) return stats;

        int totalTime = 0;
        int meanTime = 0;
        int maxTime = 0;
        int minTime = 0;

        try {
            ResultSet rs = sqler.GetSearchResultsBySize(size);
            maxTime = 0;
            minTime = Integer.MAX_VALUE;
            
            while (rs.next()) {
                int taskTimeSeconds = rs.getInt("runTime");

                if(rs.getString("startTime") != null) {
                    taskTimeSeconds += CalculateTimeDifference(rs.getString("name"));
                }

                totalTime += taskTimeSeconds;
                if(taskTimeSeconds < minTime) {
                    minTime = taskTimeSeconds;
                }
                if(taskTimeSeconds > maxTime) {
                    maxTime = taskTimeSeconds;
                }
            }
            rs.close();
    
            meanTime = totalTime/numTasks;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        stats.put("totalTime",totalTime);
        stats.put("meanTime",meanTime);
        stats.put("maxTime",maxTime);
        stats.put("minTime",minTime);
        stats.put("numTasks",numTasks);

        return stats;
    }

    public HashMap<String, Integer> FullSummaryStatistics() {
        HashMap<String, Integer> stats = new HashMap<>();
        int numTasks = CountAllTasks();
        if(numTasks < 2) return stats;

        int totalTime = 0;
        int meanTime = 0;
        int maxTime = 0;
        int minTime = Integer.MAX_VALUE;


        List<String> sizes = Arrays.asList( "S", "M", "L", "XL");

        for(String size : sizes) {
            HashMap<String, Integer> sizeStats = SizeSummaryStatistics(size);
            if(sizeStats.isEmpty()) {
                continue;
            }

            totalTime += sizeStats.get("totalTime");
            if(sizeStats.get("minTime") < minTime) {
                minTime = sizeStats.get("minTime");
            }
            if(sizeStats.get("maxTime") > maxTime) {
                maxTime = sizeStats.get("maxTime");
            }
        }

        meanTime = totalTime/numTasks;

        stats.put("totalTime",totalTime);
        stats.put("meanTime",meanTime);
        stats.put("maxTime",maxTime);
        stats.put("minTime",minTime);
        stats.put("numTasks",numTasks);

        return stats;
    }

    public void StartTask(String taskName) {
        LocalDateTime startTimeLDT = LocalDateTime.now();
        DateTimeFormatter dtf = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss"); 

        sqler.StartTask(taskName, dtf.format(startTimeLDT));
    }

    public void StopTask(String taskName) {
        int timeDifference = CalculateTimeDifference(taskName);

        sqler.IncrementRuntime(taskName, timeDifference);

        sqler.DeleteStartTime(taskName);
    }

    public int CalculateTimeDifference(String taskName) {
        DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss");
        LocalDateTime startTimeLDT = sqler.GetStartTimeObject(taskName, dtf);
        LocalDateTime stopTimeLDT = LocalDateTime.now();

        Duration duration = Duration.between(stopTimeLDT, startTimeLDT);
        return (int)Math.abs(duration.getSeconds());
    }

    public int GetRuntime(String taskName) {
        int runTime = 0;
        try {
            ResultSet rs = GetTask(taskName);
            runTime = rs.getInt("runTime");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return runTime;
    }

    public int CountAllTasks() {
        return sqler.CountAllTasks();
    }

    public int CountSizeClass(String size) {
        if(size.isEmpty()) return sqler.CountAllTasks();
        return sqler.GetSizeClassSize(size);
    }

    public void RenameTask(String oldTaskName, String newTaskName) {
        sqler.RenameTask(oldTaskName, newTaskName);
    }

    public void ResizeTask(String taskName, String newSize) {
        sqler.ResizeTask(taskName, newSize);
    }

    public void SetTaskDescription(String taskName, String description) {
        sqler.SetTaskDescription(taskName, description);
    }

    public String GenerateTimeStringFromSeconds(int timeInSeconds) {
        int hours = timeInSeconds/3600;
        int minutes = (timeInSeconds % 3600)/60;
        int seconds = (timeInSeconds % 3600)%60;

        return hours == 0 ?
                String.format("%02d:%02d",minutes,seconds)
                : String.format("%d:%02d:%02d",hours,minutes,seconds);
    }

    private Data sqler;
}