import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Arrays;


public class CustomerAgent extends Agent {
	
	private double minCost;
	private double maxCost;
	
	private int[] lastProposal ;
	private int lastProposalRound;

	private int[][] timeMatrix;

	public CustomerAgent(File file, int minCost, int maxCost) throws FileNotFoundException {
		
		
		Scanner scanner = new Scanner(file);
		int jobs = scanner.nextInt();
		int machines = scanner.nextInt();
		timeMatrix = new int[jobs][machines];
		for (int i = 0; i < timeMatrix.length; i++) {
			for (int j = 0; j < timeMatrix[i].length; j++) {
				int x = scanner.nextInt();
				timeMatrix[i][j] = x;
			}
		}

		scanner.close();
		
		this.minCost = (double)minCost;
		this.maxCost = (double)maxCost;
		
		this.lastProposal = new int[timeMatrix.length];
		this.lastProposalRound = 0;
	}

	public boolean vote(int[] contract, int[] proposal) {
		int timeContract = evaluate(contract);
		int timeProposal = evaluate(proposal);

		double val = ((double)timeProposal-minCost) / (maxCost-minCost);
		if ((val > 1.0) || (val < 0.0)) {
			System.out.println("ERROR, val is out of bounds: " + val);
		}
		double randomNumber = Math.random();
		
		return (randomNumber < val);
	}
	
	public boolean voteScore(int[] proposal) {
		int timeProposal = evaluate(proposal);

		double val = ((double)timeProposal-minCost) / (maxCost-minCost);
		if ((val > 1.0) || (val < 0.0)) {
			System.out.println("ERROR, val is out of bounds: " + val);
		}
		double randomNumber = Math.random();
		
		return (randomNumber < val);
	}
	
	public double getScore(int[] proposal) {
		int timeProposal = evaluate(proposal);
		
		double val = ((double)timeProposal-minCost) / (maxCost-minCost);
		if (val < 0.0) {
			System.out.println("ERROR, val is out of bounds: " + val);
			val = 0.0;
		}
		if (val > 1.0) {
			val = 1.0;
		}
		return val;
	}
	
	public boolean voteScoreBinary(int[] proposal) {		
		if (!Arrays.equals(proposal, lastProposal)) {
			lastProposal = proposal;
			lastProposalRound = 1;
		}
		else {
			lastProposalRound += 1;
		}
		
		double score = getScore(proposal);
		return getValueAtIteration(score, lastProposalRound-1);	
	}	
	
	public int getContractSize() {
		return timeMatrix.length;
	}

	public void printUtility(int[] contract) {
		System.out.print(evaluate(contract));
	}
	
	public int getUtility(int[] contract) {
		return evaluate(contract);
	}

	
	private int evaluate(int[] solution) {
		int anzM = timeMatrix[0].length;
		
		if(timeMatrix.length != solution.length)System.out.println("Fehler in ");
		int[][] start = new int[timeMatrix.length][timeMatrix[0].length];

		for(int i=0;i<start.length;i++) {
			for(int j=0;j<start[i].length;j++) {
				start[i][j] = 0;
			}
		}
		
		int job = solution[0];
		for(int m=1;m<anzM;m++) {
			start[job][m] = start[job][m-1] + timeMatrix[job][m-1];
		}
		
		for(int j=1;j<solution.length;j++) {
			int delay             = 0;
			int vorg              = solution[j-1];
			job                   = solution[j];
			boolean delayErhoehen;  
			do {
				delayErhoehen = false;
				start[job][0] = start[vorg][0] + timeMatrix[vorg][0] + delay;
				for(int m=1;m<anzM;m++) {					
					start[job][m] = start[job][m-1] + timeMatrix[job][m-1];
					if(start[job][m] < start[vorg][m]+timeMatrix[vorg][m]) {
						delayErhoehen = true;
						delay++;
						break;
					}
				}
			}while(delayErhoehen);
		}
		int last = solution[solution.length-1];
		
		
//		for(int j=0;j<solution.length;j++) {
//			for(int m=0;m<anzM;m++) {
//				System.out.print(start[j][m] + "\t");
//			}
//			System.out.println();
//		}
		
		return (start[last][anzM-1]+timeMatrix[last][anzM-1]);
	}
	
	
	public boolean getValueAtIteration(double value, int targetIteration) {
        double lowerBound = 0.0;
        double upperBound = 1.0;

        for (int i = 0; i < targetIteration; i++) {
            double mid = (lowerBound + upperBound) / 2.0;

            if (value >= mid) {
                lowerBound = mid; // Update lower bound for the next iteration
            } else {
                upperBound = mid; // Update upper bound for the next iteration
            }
        }

        return value >= (lowerBound + upperBound) / 2.0;
    }

	
}
