package scheduler;

public class Process {
    private static char ID = 'A';
    private char jobID;
    private int jobLength;
    private int originalLength; // To store initial job length
    private int tickets;
    private int allotmentCount = 0;
    private int arrivalTime;
    // For stride scheduling
    private double pass = 0;
    private double stride;
    private static final double STRIDE_MULTIPLIER = 10000.0;
    // For timing metrics
    private int firstResponseTime = -1;
    private int completionTime = -1;

    public Process() {
        jobID = ID++;
        jobLength = 0;
        originalLength = jobLength;
        tickets = 0;
        arrivalTime = 0;
        initializeStride();
    }

    public Process(int minLength, int maxLength,
                  int earliestArrival, int latestArrival,
                  int minTickets, int maxTickets) {
        jobID = ID++;
        jobLength = 10*((int)(Math.random()*((maxLength/10)-(minLength/10)))+(minLength/10));
        originalLength = jobLength;
        arrivalTime = 10*(int)(Math.random()*((latestArrival/10)-(earliestArrival/10))+(earliestArrival/10));
        tickets = (int)(Math.random()*(maxTickets-minTickets))+minTickets;
        initializeStride();
    }

    private void initializeStride() {
        this.stride = STRIDE_MULTIPLIER / tickets;
    }

    public char getJobID() {
        return jobID;
    }

    public int getJobLength() {
        return jobLength;
    }

    public int getOriginalLength() {
        return originalLength;
    }

    public void setJobLength(int jLength) {
        jobLength = jLength;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int num) {
        tickets = num;
    }

    public int getAllotmentCount() {
        return allotmentCount;
    }

    public void resetAllotmentCount() {
        allotmentCount = 0;
    }

    public void incAllotmentCount() {
        allotmentCount++;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int start) {
        arrivalTime = start;
    }

    public double getPass() {
        return pass;
    }

    public void setPass(double pass) {
        this.pass = pass;
    }

    public void updatePass() {
        pass += stride;
    }

    public double getStride() {
        return stride;
    }

    public void setFirstResponseTime(int time) {
        if (firstResponseTime == -1) {
            firstResponseTime = time;
        }
    }

    public int getFirstResponseTime() {
        return firstResponseTime;
    }

    public void setCompletionTime(int time) {
        completionTime = time;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnaroundTime() {
        if (completionTime == -1) return -1;
        return completionTime - arrivalTime;
    }

    public int getResponseTime() {
        if (firstResponseTime == -1) return -1;
        return firstResponseTime - arrivalTime;
    }

    public void resetTimingMetrics() {
        firstResponseTime = -1;
        completionTime = -1;
    }

    public void printProcess() {
        System.out.println("Process: "+jobID+" Arrival Time: "+arrivalTime+
                         " Job Length: "+jobLength+" Tickets: "+tickets+
                         " Stride: "+String.format("%.2f", stride));
    }

}
