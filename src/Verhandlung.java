import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Verhandlung {

	private static int i1 = 0;
	private static int i2 = 1;
	private static int i3 = 2;

	public static void main(String[] args) {
		boolean logging = false;

		int maxRounds = 10000;
		int voteAccuracy = 16;

		// TODO: automatically set min/max cost parameters
		int minCost = 1;
		int maxCost = 12000;

		double[] costWeighting = { 1.0, 1.0 };

		long timestamp = System.currentTimeMillis();
		System.out.println("Starting negotiation at " + timestamp);
		String logFile = "logs/log_" + Long.toString(timestamp) + ".txt";
		if (logging) logParameters(logFile, minCost, maxCost, maxRounds, voteAccuracy);

		String saveFile = "saves/saves_" + Long.toString(timestamp) + ".csv";
		writeString(saveFile, "utilA;utilB;utilSum;contract");

		List<UtilContract> contractHistory = loadFromCSV("saves/pareto.csv");
		System.out.println("Loaded " + contractHistory.size() + " contracts from file");
		int[] contract = getBestContract("O"); // "A","B","OVERALL"/"O"


		Mediator med = null;
		Agent agA = null, agB = null;
		try {
			agA = new SupplierAgent(new File("data/daten3ASupplier_200.txt"), minCost, maxCost);
			agB = new CustomerAgent(new File("data/daten4BCustomer_200_5.txt"), minCost, maxCost);
			med = new Mediator(agA.getContractSize(), agB.getContractSize());

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}

		assert med != null && agA != null && agB != null;
		int bestCost = agA.getUtility(contract) + agB.getUtility(contract);

		if (logging) writeString(logFile, "Created new proposal:" + getStringFromArray(contract));
		if (logging) logExactScores(logFile, agA.getScore(contract), agB.getScore(contract));

		double[] scores = getBinaryScores(logFile, agA, agB, contract, 1000000, voteAccuracy, logging);
		double bestScore = scores[1] * costWeighting[0] + scores[3] * costWeighting[1];
		// printNewBest(0, agA, agB, contract, scores);
		runNegotiation(med, agA, agB, contract, bestCost, bestScore, maxRounds, saveFile, logFile, logging, contractHistory);
	}

	public static void runNegotiation(Mediator med, Agent agA, Agent agB, int[] contract, int bestCost,
			double bestScore, int maxRounds, String saveFile, String logFile, boolean logging,
			List<UtilContract> contractHistory) {

		int[] lowestExplorationContract = contract;

		for (int round = 1; round < maxRounds; round++) {

			int lowestExploration = 999_999;
			int lowestExplorationIndex = 0;
			UtilContract lowestExplorationUtilContract = null;

			for (UtilContract c : contractHistory) {
				if (c.getExplored() < lowestExploration) {
					lowestExploration = c.getExplored();
					lowestExplorationContract = c.getContract();
					lowestExplorationIndex = contractHistory.indexOf(c);
					lowestExplorationUtilContract = c;
					resetIndex();
				}
			}

			System.out.println(round + " CONTRACT " + lowestExplorationUtilContract.getUtilA() + " + " + lowestExplorationUtilContract.getUtilb() + " | EXPLORATION "  + lowestExploration);
			
			while (i1 != contract.length) {
				int proposal[] = null;
				
				if (lowestExploration == 0) {
					System.out.println("Found exploration 0");
					break;
				}
				else if (lowestExploration == 1) {
					proposal = constructNextProposal(lowestExplorationContract);
				}
				else if (lowestExploration == 2) {
					proposal = constructNextProposal3(lowestExplorationContract);
				}
				else {
					System.out.println("Reached exploration " + lowestExploration);
					break;
				}
				assert proposal != null;
				int utilA = agA.getUtility(proposal);
				int utilB = agB.getUtility(proposal);
				
				UtilContract newContract = new UtilContract(utilA, utilB, utilA + utilB, proposal);
				

				if (isParetoEfficient(contractHistory, newContract)) {
					System.out.print("NEW CONTRACT " + utilA + " + " + utilB + " = " + (utilA + utilB) + " [" + contractHistory.size() + "]");
					saveContract(saveFile, proposal, utilA, utilB);
					contractHistory.add(newContract);

					UtilContract el = contractHistory.get(lowestExplorationIndex);
					contractHistory = removeNonPareto(contractHistory);
					lowestExplorationIndex = contractHistory.indexOf(el);
					

					if (lowestExplorationIndex == -1) {
						System.out.println(". Aborting old contract");
						break;
					}
					else {
						System.out.println(". Continuing old contract");
					}
				}
				
				// if (agA.getUtility(proposal) + agB.getUtility(proposal) < bestCost) {
				// 	bestCost = agA.getUtility(proposal) + agB.getUtility(proposal);
				// 	contract = proposal;
				// 	System.out.println("New best contract found: " + bestCost);
				// 	med.resetIndex();
				// }
	
				if (logging) writeString(logFile, "Created new proposal:" + getStringFromArray(proposal));
				if (logging) logExactScores(logFile, agA.getScore(proposal), agB.getScore(proposal));
				
				contractHistory.get(lowestExplorationIndex).setExplored(lowestExploration + 1);
				
			}

			

			// if eploration depth is reached, update exloration
			// check exploration of contracts and choose the lowest exploration / the lowest utilSum
			// reset index

			

			

			// scores = getBinaryScores(logFile, agA, agB, proposal, bestScore,
			// voteAccuracy, logging);

			// if (scores[1] * costWeighting[0] + scores[3] * costWeighting[1] < bestScore)
			// { // max scores are better than
			// // best score
			// contract = proposal;
			// bestScore = scores[1] * costWeighting[0] + scores[3] * costWeighting[1];
			// printNewBest(round, agA, agB, contract, scores);
			// }

			if (round % 1000 == 0) {
				System.out.print(".");
			}
		}

		System.out.println("DONE");
	}

	public static void saveContractHistory(String saveFile, List<UtilContract> contractHistory) {
		for (UtilContract contract : contractHistory) {
			saveContract(saveFile, contract.getContract(), contract.getUtilA(), contract.getUtilB());
		}
	}

	public static double[] getBinaryScores(String logFile, Agent agA, Agent agB, int[] proposal, double bestScore,
			int voteAccuracy, boolean logging) {
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
			} else {
				maxScoreA -= (maxScoreA - minScoreA) / 2;
			}

			boolean voteB = agB.voteScoreBinary(proposal);
			if (voteB) {
				minScoreB += (maxScoreB - minScoreB) / 2;
			} else {
				maxScoreB -= (maxScoreB - minScoreB) / 2;
			}

			if (((maxScoreA + maxScoreB) < bestScore) && (round >= voteAccuracy)) { // break only when accuracy is
																					// achieved
				break;
			}
			round += 1;

			scores = new double[] { minScoreA, maxScoreA, minScoreB, maxScoreB };
			ScoredContract scoredContract = new ScoredContract(proposal, scores);
			if (logging)
				logRound(logFile, round, scoredContract);

		}

		return scores;
	}

	public static void printNewBest(int round, Agent agA, Agent agB, int[] contract, double[] scores) {
		System.out.println("NEW BEST CONTRACT IN ROUND " + round);
		System.out.println("ACTUAL SCORES    | A: " + String.format("%.6f", agA.getScore(contract)) + " | B: "
				+ String.format("%.6f", agB.getScore(contract)));
		System.out.println(
				"PREDICTED SCORES | A: [ " + String.format("%.4f", scores[0]) + " > " + String.format("%.4f", scores[1])
						+ " ]" +
						" B: [ " + String.format("%.4f", scores[2]) + " > " + String.format("%.4f", scores[3]) + " ]");
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
		writeString(saveFile, utilA + ";" + utilB + ";" + (utilA + utilB) + ";" + getStringFromArray(contract));
	}

	public static String getStringFromArray(int[] arr) {
		String s = "[";
		for (int i = 0; i < arr.length; i++) {
			s += arr[i] + ",";
		}
		s = s.substring(0, s.length() - 1);
		s += "]";
		return s;
	}

	public static int[] getArrayFromString(String input) {
		String[] parts = input.replace("[", "").replace("]", "").split(",");
		int[] result = new int[parts.length];

		// Convert each string element to int
		for (int i = 0; i < parts.length; i++) {
			result[i] = Integer.parseInt(parts[i].trim());
		}

		return result;
	}

	public static int[] getBestContract(String scoring) {
		assert (scoring == "A" || scoring == "B" || scoring == "OVERALL");
		int[] contract = null;
		int bestScore = 999_999;

		try {
			FileReader fr = new FileReader("saves/pareto.csv");
			BufferedReader br = new BufferedReader(fr);

			String line;
			br.readLine(); // skip header
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] row = line.split(";");
				int data = 1_000_000;

				if (scoring == "A") {
					data = Integer.parseInt(row[0]);
				} else if (scoring == "B") {
					data = Integer.parseInt(row[1]);
				} else if (scoring == "OVERALL" || scoring == "O") {
					data = Integer.parseInt(row[2]);
				}

				if (data < bestScore) {
					bestScore = data;
					contract = getArrayFromString(row[3]);
				}
			}

			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Loaded best contract for " + scoring + " with score " + bestScore + "\n");
		return contract;
	}

	public static int[] constructNextProposal(int[] contract) {
		int[] proposal = new int[contract.length];
		for (int i = 0; i < proposal.length; i++)
			proposal[i] = contract[i];

		int temp = proposal[i1];
		proposal[i1] = proposal[i2];
		proposal[i2] = temp;

		i2++;
		if (i2 == proposal.length) {
			i1++;
			i2 = Math.min((i1 + 1), proposal.length - 1);
		}

		return proposal;
	}

	public static int[] constructNextProposal3(int[] contract) {
		int[] proposal = new int[contract.length];
		for (int i = 0; i < proposal.length; i++)
			proposal[i] = contract[i];

		int temp = proposal[i1];
		proposal[i1] = proposal[i2];
		proposal[i2] = temp;

		int temp2 = proposal[i2];
		proposal[i2] = proposal[i3];
		proposal[i3] = temp2;

		i3++;
		if (i3 == proposal.length) {
			i2++;
			i3 = Math.min((i2 + 1), proposal.length - 1);
		}
		if (i2 == proposal.length) {
			i1++;
			i2 = Math.min((i1 + 1), proposal.length - 1);
			i3 = Math.min((i2 + 1), proposal.length - 1);
		}

		return proposal;
	}

	public static void resetIndex() {
		i1 = 0;
		i2 = 1;
		i3 = 2;
	}

	public static boolean isParetoEfficient(List<UtilContract> paretoElements, UtilContract newElement) {
        for (UtilContract element : paretoElements) {
            if (newElement.getUtilA() >= element.getUtilA() && newElement.getUtilB() >= element.getUtilB()) {
                return false; 
            }
        }
        return true;
    }

	public static boolean isParetoEfficientOrSame(List<UtilContract> paretoElements, UtilContract newElement) {
        for (UtilContract element : paretoElements) {
            if (newElement.getUtilA() > element.getUtilA() && newElement.getUtilB() > element.getUtilB()) {
                return false; 
            }
        }
        return true;
    }

	public static List<UtilContract> removeNonPareto(List<UtilContract> paretoElements) {
        List<UtilContract> newParetoElements = new ArrayList<>();

        for (UtilContract element : paretoElements) {
            boolean isPareto = true;
            for (UtilContract otherElement : paretoElements) {
				if (element == otherElement) continue;
                if (element.getUtilA() >= otherElement.getUtilA() && element.getUtilB() >= otherElement.getUtilB()) {
                    isPareto = false;
                    break;
                }
            }
            if (isPareto) {
                newParetoElements.add(element);
            }
        }
        return newParetoElements;
    }

	public static List<UtilContract> loadFromCSV(String filePath) {
        List<UtilContract> contractList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
			br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) { // Ensure correct format
                    int utilA = Integer.parseInt(parts[0]);
                    int utilB = Integer.parseInt(parts[1]);
                    int utilSum = Integer.parseInt(parts[2]);
                    int[] contract = getArrayFromString(parts[3]);
                    contractList.add(new UtilContract(utilA, utilB, utilSum, contract, 1)); // Set explored to 1
                } else {
                    System.out.println("Invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contractList;
    }
}