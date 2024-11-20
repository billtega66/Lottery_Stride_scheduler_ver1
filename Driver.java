//package scheduler;
//
//import java.util.*;
//
//public class Driver {
//    private static final int SIMULATION_TIME = 1000; // Total simulation time
//    private JobGenerator jG;
//    private Process[] jobList;
//
//    public Driver() {
//        jG = new JobGenerator();
//    }
//
//    public static void main(String[] args) {
//        new Driver().runCode();
//    }
//
//    public void runCode() {
//        // Generate processes with the given parameters
//        jobList = jG.generateProcesses(27, 50, 80, 0, 50, 10, 100);
//        
//        System.out.println("Initial Job List:");
//        for (Process process : jobList) {
//            process.printProcess();
//        }
//
//        // Run Lottery Scheduler
//        System.out.println("\n=== Lottery Scheduler Results ===");
//        runLotteryScheduler();
//
//        // Reset allotment counts
//        for (Process process : jobList) {
//            process.resetAllotmentCount();
//            process.setPass(0); // Reset pass values for stride scheduling
//        }
//
//        // Print Stride Values before running Stride Scheduler
//        System.out.println("\n=== Stride Values ===");
//        for (Process process : jobList) {
//            System.out.printf("Process %c:\n", process.getJobID());
//            System.out.printf("  Tickets: %d\n", process.getTickets());
//            System.out.printf("  Stride Value: %.2f\n\n", process.getStride());
//        }
//
//        // Run Stride Scheduler
//        System.out.println("\n=== Stride Scheduler Results ===");
//        runStrideScheduler();
//    }
//
//    private void runLotteryScheduler() {
//        int currentTime = 0;
//        Random random = new Random();
//        ArrayList<Process> activeProcesses = new ArrayList<>();
//
//        while (currentTime < SIMULATION_TIME) {
//            // Add newly arrived processes
//            for (Process process : jobList) {
//                if (process.getArrivalTime() == currentTime && process.getJobLength() > 0) {
//                    activeProcesses.add(process);
//                }
//            }
//
//            if (!activeProcesses.isEmpty()) {
//                // Calculate total tickets
//                int totalTickets = getTotalTickets(activeProcesses);
//                
//                // Select winning ticket
//                int winningTicket = random.nextInt(totalTickets);
//                int ticketSum = 0;
//                Process selectedProcess = null;
//
//                // Find the winning process
//                for (Process process : activeProcesses) {
//                    ticketSum += process.getTickets();
//                    if (winningTicket < ticketSum) {
//                        selectedProcess = process;
//                        break;
//                    }
//                }
//
//                // Execute the selected process
//                if (selectedProcess != null) {
//                    selectedProcess.incAllotmentCount();
//                    selectedProcess.setJobLength(selectedProcess.getJobLength() - 1);
//
//                    // Remove completed processes
//                    if (selectedProcess.getJobLength() <= 0) {
//                        activeProcesses.remove(selectedProcess);
//                    }
//                }
//            }
//            currentTime++;
//        }
//
//        printSchedulingResults("Lottery");
//    }
//
//    private void runStrideScheduler() {
//        int currentTime = 0;
//        ArrayList<Process> activeProcesses = new ArrayList<>();
//
//        while (currentTime < SIMULATION_TIME) {
//            // Add newly arrived processes
//            for (Process process : jobList) {
//                if (process.getArrivalTime() <= currentTime && 
//                    process.getJobLength() > 0 && 
//                    !activeProcesses.contains(process)) {
//                    activeProcesses.add(process);
//                }
//            }
//
//            if (!activeProcesses.isEmpty()) {
//                // Find process with lowest pass value
//                Process selectedProcess = Collections.min(activeProcesses, 
//                    Comparator.comparingDouble(Process::getPass));
//
//                // Execute the selected process
//                selectedProcess.incAllotmentCount();
//                selectedProcess.setJobLength(selectedProcess.getJobLength() - 1);
//                selectedProcess.updatePass();
//
//                // Remove completed processes
//                if (selectedProcess.getJobLength() <= 0) {
//                    activeProcesses.remove(selectedProcess);
//                }
//            }
//            currentTime++;
//        }
//
//        printSchedulingResults("Stride");
//    }
//
//    private void printSchedulingResults(String schedulerType) {
//        int totalAllotments = 0;
//        for (Process process : jobList) {
//            totalAllotments += process.getAllotmentCount();
//        }
//
//        for (Process process : jobList) {
//            double expectedPercentage = (double) process.getTickets() / 
//                getTotalTickets(Arrays.asList(jobList)) * 100;
//            double actualPercentage = (double) process.getAllotmentCount() / 
//                totalAllotments * 100;
//            
//            System.out.printf("Process %c:\n", process.getJobID());
//            System.out.printf("  Tickets: %d\n", process.getTickets());
//            if (schedulerType.equals("Stride")) {
//                System.out.printf("  Stride Value: %.2f\n", process.getStride());
//                System.out.printf("  Final Pass Value: %.2f\n", process.getPass());
//            }
//            System.out.printf("  Expected CPU %%: %.2f%%\n", expectedPercentage);
//            System.out.printf("  Actual CPU %%: %.2f%%\n", actualPercentage);
//            System.out.printf("  Total Executions: %d\n\n", 
//                process.getAllotmentCount());
//        }
//    }
//
//    private int getTotalTickets(Collection<Process> processes) {
//        int total = 0;
//        for (Process process : processes) {
//            total += process.getTickets();
//        }
//        return total;
//    }
//}

package scheduler;

import java.util.*;

public class Driver {
    private static final int SIMULATION_TIME = 1000; // Total simulation time
    private JobGenerator jG;
    private Process[] jobList;

    public Driver() {
        jG = new JobGenerator();
    }

    public static void main(String[] args) {
        new Driver().runCode();
    }

    public void runCode() {
        // Generate processes with the given parameters
        jobList = jG.generateProcesses(27, 50, 80, 0, 50, 10, 100);
        
        System.out.println("Initial Job List:");
        for (Process process : jobList) {
            process.printProcess();
        }

        // Run Lottery Scheduler
        System.out.println("\n=== Lottery Scheduler Results ===");
        runLotteryScheduler();

        // Reset for Stride Scheduler
        resetProcesses();

        // Run Stride Scheduler
        System.out.println("\n=== Stride Scheduler Results ===");
        runStrideScheduler();
    }

    private void resetProcesses() {
        for (Process process : jobList) {
            process.resetAllotmentCount();
            process.setJobLength(process.getOriginalLength());
            process.resetTimingMetrics();
        }
    }

    private void runLotteryScheduler() {
        int currentTime = 0;
        Random random = new Random();
        ArrayList<Process> activeProcesses = new ArrayList<>();

        while (currentTime < SIMULATION_TIME && hasUnfinishedJobs()) {
            // Add newly arrived processes
            for (Process process : jobList) {
                if (process.getArrivalTime() == currentTime && process.getJobLength() > 0) {
                    activeProcesses.add(process);
                }
            }

            if (!activeProcesses.isEmpty()) {
                // Calculate total tickets
                int totalTickets = getTotalTickets(activeProcesses);
                
                // Select winning ticket
                int winningTicket = random.nextInt(totalTickets);
                int ticketSum = 0;
                Process selectedProcess = null;

                // Find the winning process
                for (Process process : activeProcesses) {
                    ticketSum += process.getTickets();
                    if (winningTicket < ticketSum) {
                        selectedProcess = process;
                        break;
                    }
                }

                // Execute the selected process
                if (selectedProcess != null) {
                    selectedProcess.setFirstResponseTime(currentTime);
                    selectedProcess.incAllotmentCount();
                    selectedProcess.setJobLength(selectedProcess.getJobLength() - 1);

                    // Check if process is completed
                    if (selectedProcess.getJobLength() <= 0) {
                        selectedProcess.setCompletionTime(currentTime);
                        activeProcesses.remove(selectedProcess);
                    }
                }
            }
            currentTime++;
        }

        printSchedulingResults("Lottery");
    }

    private void runStrideScheduler() {
        int currentTime = 0;
        ArrayList<Process> activeProcesses = new ArrayList<>();

        while (currentTime < SIMULATION_TIME && hasUnfinishedJobs()) {
            // Add newly arrived processes
            for (Process process : jobList) {
                if (process.getArrivalTime() <= currentTime && 
                    process.getJobLength() > 0 && 
                    !activeProcesses.contains(process)) {
                    activeProcesses.add(process);
                }
            }

            if (!activeProcesses.isEmpty()) {
                // Find process with lowest pass value
                Process selectedProcess = Collections.min(activeProcesses, 
                    Comparator.comparingDouble(Process::getPass));

                // Execute the selected process
                selectedProcess.setFirstResponseTime(currentTime);
                selectedProcess.incAllotmentCount();
                selectedProcess.setJobLength(selectedProcess.getJobLength() - 1);
                selectedProcess.updatePass();

                // Check if process is completed
                if (selectedProcess.getJobLength() <= 0) {
                    selectedProcess.setCompletionTime(currentTime);
                    activeProcesses.remove(selectedProcess);
                }
            }
            currentTime++;
        }

        printSchedulingResults("Stride");
    }

    private boolean hasUnfinishedJobs() {
        for (Process process : jobList) {
            if (process.getJobLength() > 0) return true;
        }
        return false;
    }

    private void printSchedulingResults(String schedulerType) {
        int totalAllotments = 0;
        double totalTurnaroundTime = 0;
        double totalResponseTime = 0;
        int completedJobs = 0;

        for (Process process : jobList) {
            totalAllotments += process.getAllotmentCount();
            if (process.getCompletionTime() != -1) {
                totalTurnaroundTime += process.getTurnaroundTime();
                totalResponseTime += process.getResponseTime();
                completedJobs++;
            }
        }

        System.out.println("\nDetailed Results for " + schedulerType + " Scheduling:");
        System.out.println("===================================================");

        for (Process process : jobList) {
            double expectedPercentage = (double) process.getTickets() / 
                getTotalTickets(Arrays.asList(jobList)) * 100;
            double actualPercentage = (double) process.getAllotmentCount() / 
                totalAllotments * 100;
            
            System.out.printf("Process %c:\n", process.getJobID());
            System.out.printf("  Tickets: %d\n", process.getTickets());
            if (schedulerType.equals("Stride")) {
                System.out.printf("  Stride Value: %.2f\n", process.getStride());
            }
            System.out.printf("  Expected CPU %%: %.2f%%\n", expectedPercentage);
            System.out.printf("  Actual CPU %%: %.2f%%\n", actualPercentage);
            System.out.printf("  Total Executions: %d\n", process.getAllotmentCount());
            System.out.printf("  Arrival Time: %d\n", process.getArrivalTime());
            
            if (process.getCompletionTime() != -1) {
                System.out.printf("  Completion Time: %d\n", process.getCompletionTime());
                System.out.printf("  Turnaround Time: %d\n", process.getTurnaroundTime());
                System.out.printf("  Response Time: %d\n", process.getResponseTime());
            } else {
                System.out.println("  Process did not complete within simulation time");
            }
            System.out.println();
        }

        if (completedJobs > 0) {
            System.out.println("Overall Statistics:");
            System.out.printf("  Average Turnaround Time: %.2f\n", 
                totalTurnaroundTime / completedJobs);
            System.out.printf("  Average Response Time: %.2f\n", 
                totalResponseTime / completedJobs);
            System.out.printf("  Completed Jobs: %d/%d\n", 
                completedJobs, jobList.length);
        }
    }

    private int getTotalTickets(Collection<Process> processes) {
        int total = 0;
        for (Process process : processes) {
            total += process.getTickets();
        }
        return total;
    }
}