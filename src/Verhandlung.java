import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Verhandlung {	
	
	
		public static void main(String[] args) {
			int maxRounds = 100000;
			int voteAccuracy = 12;

			String proposalCreation = "switched"; // "switched" or "random"
			
			// TODO: automatically set min/max cost parameters
			int minCost = 100;
			int maxCost = 12000;
			
			long timestamp = System.currentTimeMillis();
			String logFile = "logs/log_" + Long.toString(timestamp) + ".txt";
			logParameters(logFile, minCost, maxCost, maxRounds, voteAccuracy);

			String saveFile = "saves/saves_"+ Long.toString(timestamp) + ".csv";
			writeString(saveFile, "utilA;utilB;utilSum;contract");
			
			Mediator med = null;
			Agent agA = null, agB = null;
			try {
				agA = new SupplierAgent(new File("data/daten3ASupplier_200.txt"), minCost, maxCost);
				agB = new CustomerAgent(new File("data/daten4BCustomer_200_5.txt"), minCost, maxCost);
				med = new Mediator(agA.getContractSize(), agB.getContractSize());
				
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
			
			// initialize baseline contract + score
			assert med != null && agA != null && agB != null;
			int[] contract = med.initContract();
			double bestScore = 10;

			// manually put in start contract here
			contract = new int[]{};
			

				
			for(int round=1;round<maxRounds;round++) {			
				int[] proposal = med.getUniqueProposal(contract, proposalCreation);	
				// int[] proposal = med.constructRandomProposal(contract);

				saveContract(saveFile, proposal, agA.getUtility(proposal), agB.getUtility(proposal));
				
				writeString(logFile, "Created new proposal:" + getArrayString(proposal));
				logExactScores(logFile, agA.getScore(proposal), agB.getScore(proposal));
				
				double[] scores = getBinaryScores(logFile, agA, agB, proposal, bestScore, voteAccuracy);	
				
				if (scores[1] + scores[3] < bestScore) { // max scores are better than best score
					contract = proposal;
					bestScore = scores[1] + scores[3];
					printNewBest(round, agA, agB, proposal, scores);
				}
			}

			System.out.println("DONE");
		}
		
		
		public static int[] getVotes(Agent agA, Agent agB, int[] proposal, int maxVoteRound) {
			int voteRound;
			
			int scoreA = 0;
			for (voteRound=0;voteRound<maxVoteRound;voteRound++) {
				scoreA += agA.voteScore(proposal) ? 1 : 0; 
			}
			
			int scoreB = 0;
			for (voteRound=0;voteRound<maxVoteRound;voteRound++) {
				scoreB += agB.voteScore(proposal) ? 1 : 0; 
			}
			
			int[] scores = {scoreA, scoreB};
			return scores;
		}
		
		public static double[] getBinaryScores(String logFile, Agent agA, Agent agB, int[] proposal, double bestScore, int voteAccuracy) {
			// get scores of both agents iteratively
			// stop when score is guaranteed better / worse than bestScore
			// [ there is a min + max bound of the score ]
			// return scores
			
			double[] scores = null;
			
			double minScoreA = 0.0;
			double maxScoreA = 1.0;
			double minScoreB = 0.0;
			double maxScoreB = 1.0;
			
			int round = 0;
			while ((minScoreA + minScoreB <= bestScore)) {

				boolean voteA = agA.voteScoreBinary(proposal);
				if (voteA) {
					minScoreA += (maxScoreA - minScoreA) / 2;
				}
				else {
					maxScoreA -= (maxScoreA - minScoreA) / 2;
				}
				
				boolean voteB = agB.voteScoreBinary(proposal);
				if (voteB) {
					minScoreB += (maxScoreB - minScoreB) / 2;
				}
				else {
					maxScoreB -= (maxScoreB - minScoreB) / 2;
				}
								
				if (((maxScoreA + maxScoreB) < bestScore) && (round >= voteAccuracy)) { // break only when accuracy is achieved
					break;
				}
				round += 1;
				
				scores = new double[]{minScoreA, maxScoreA, minScoreB, maxScoreB};
				ScoredContract scoredContract = new ScoredContract(proposal, scores);
				logRound(logFile, round, scoredContract);
				
			}
			
			return scores;
		}
		
		public static void printNewBest(int round, Agent agA, Agent agB, int[] contract, double[] scores) {			
			System.out.println("NEW BEST CONTRACT IN ROUND " + round);
			System.out.println("ACTUAL SCORES    | A: " + String.format("%.6f", agA.getScore(contract)) + " | B: " + String.format("%.6f", agB.getScore(contract)));
			System.out.println(
					"PREDICTED SCORES | A: [ " + String.format("%.4f", scores[0]) + " > " + String.format("%.4f", scores[1]) + " ]" + 
					" B: [ " + String.format("%.4f", scores[2]) + " > " + String.format("%.4f", scores[3]) + " ]"
			);
			System.out.println("BEST SCORE       | " + String.format("%.6f", scores[1] + scores[3]));
			int utilA = agA.getUtility(contract);
			int utilB = agB.getUtility(contract);
			System.out.println("UTILITY          | " + utilA + " + " + utilB + " = " + (utilA + utilB));
			System.out.println();
		}	
		
		public static void writeString(String logFile, String text) {
			try {
	            FileWriter fw = new FileWriter(logFile, true);
	            BufferedWriter bw = new BufferedWriter(fw);

	            bw.write(text);
	            bw.newLine();

	            bw.close();
	            fw.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		public static void logRound(String logFile, int round, ScoredContract scoredContract) {
	        writeString(logFile, "ROUND " + round + " : " + scoredContract.getString());
	    }
		
		public static void logParameters(String logFile, int minCost, int maxCost, int maxRounds, int voteAccuracy) {
			String s = "--- PARAMETERS ---\n" + 
						"COST: " + minCost + " -> " + maxCost + "\n" + 
						"MAX ROUNDS: " + maxRounds + "\n" + 
						"VOTE ACCURACY: " + voteAccuracy + "\n" + 
						"--- PARAMETERS ---\n" + 
						"\n";
			writeString(logFile, s);
	    }
		
		public static void logExactScores(String logFile, double scoreA, double scoreB) {
			writeString(logFile, "A:" + String.format("%.8f", scoreA) + " | B: " + String.format("%.8f", scoreB));
	    }

		public static void saveContract(String saveFile, int[] contract, int utilA, int utilB) {
			writeString(saveFile, utilA + ";" + utilB + ";" + (utilA + utilB) + ";" + getArrayString(contract));
	    }

		public static String getArrayString(int[] arr) {
	        String s = "[";
			for (int i = 0; i < arr.length; i++) {
	            s += arr[i] + ",";
	        }
			s += "]";
			return s;
	    }
}