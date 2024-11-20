package scheduler;

public class JobGenerator {

	public Process[] generateProcesses(int numJobs, int minLength, int maxLength, 
			int earliestArrival, int latestArrival, int minTickets, int maxTickets
			) {
		
		Process[] processList = new Process[numJobs];
		
		for (int index=0; index<numJobs; index++) {
			processList[index] = new Process(minLength, maxLength, 
											 earliestArrival, latestArrival,
											 minTickets, maxTickets);
											
		}
		return processList;
	}
}
