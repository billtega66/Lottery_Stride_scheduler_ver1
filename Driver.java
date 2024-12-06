package scheduler;

import java.util.*;

public class Driver {
    private static final int SIMULATION_TIME = 1000; // Total simulation time
    private static final int TIME_SLICE = 10; // Time slice set to 10 ms
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
        jobList = jG.generateProcesses(5, 10, 120, 0, 100, 10, 100);

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
        }
    }

    private void runLotteryScheduler() {
        int currentTime = 0;
        Random random = new Random();
        ArrayList<Process> activeProcesses = new ArrayList<>();
        StringBuilder executionOrder = new StringBuilder("Order of execution: ");

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
                    selectedProcess.incAllotmentCount();
                    int remainingJobLength = selectedProcess.getJobLength() - TIME_SLICE;
                    selectedProcess.setJobLength(Math.max(remainingJobLength, 0));
                    executionOrder.append(selectedProcess.getJobID()).append(" | ");

                    // Check if process is completed
                    if (selectedProcess.getJobLength() <= 0) {
                        activeProcesses.remove(selectedProcess);
                    }
                }
            }
            currentTime += TIME_SLICE; // Increment by time slice
        }

        System.out.println(executionOrder.toString());
        printSchedulingResults("Lottery");
    }

    private void runStrideScheduler() {
        int currentTime = 0;
        ArrayList<Process> activeProcesses = new ArrayList<>();
        StringBuilder executionOrder = new StringBuilder("Order of execution: ");

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
                selectedProcess.incAllotmentCount();
                int remainingJobLength = selectedProcess.getJobLength() - TIME_SLICE;
                selectedProcess.setJobLength(Math.max(remainingJobLength, 0));
                selectedProcess.updatePass();
                executionOrder.append(selectedProcess.getJobID()).append(" | ");

                // Check if process is completed
                if (selectedProcess.getJobLength() <= 0) {
                    activeProcesses.remove(selectedProcess);
                }
            }
            currentTime += TIME_SLICE; // Increment by time slice
        }

        System.out.println(executionOrder.toString());
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

        for (Process process : jobList) {
            totalAllotments += process.getAllotmentCount();
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
            System.out.printf("  Total Executions: %d\n\n", process.getAllotmentCount());
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
