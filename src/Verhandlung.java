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

	private static boolean useScoring = false;

	private static boolean logging = false;
	private static int numberOfRounds = 1000;

	// --- scoring parameters ---
	private static int minCost = 1;
	private static int maxCost = 100000; // TODO: automatically set maxCost
	private static int voteAccuracy = 16;
	// --- scoring parameters ---

	private static String logFile;
	private static String saveFile;

	private static Mediator med;
	private static Agent agA, agB;

	private static List<UtilContract> utilHistory;
	private static List<ScoreContract> scoreHistory;

	// initialize exploration indices
	private static int i1 = 0;
	private static int i2 = 1;
	private static int i3 = 2;

	public static void main(String[] args) {

		long timestamp = System.currentTimeMillis();
		System.out.println("Starting negotiation at " + timestamp);

		logFile = "logs/log_" + Long.toString(timestamp) + ".txt";
		if (logging) logParameters(logFile, minCost, maxCost, numberOfRounds, voteAccuracy);

		saveFile = "saves/saves_" + Long.toString(timestamp) + ".csv";
		writeString(saveFile, "utilA;utilB;utilSum;contract");

		// --- setup Agents and Mediator ---
		try {
			agA = new SupplierAgent(new File("data/daten3ASupplier_200.txt"), minCost, maxCost);
			agB = new CustomerAgent(new File("data/daten4BCustomer_200_5.txt"), minCost, maxCost);
			med = new Mediator(agA.getContractSize(), agB.getContractSize());

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		assert med != null && agA != null && agB != null;
		// --- setup Agents and Mediator ---

		utilHistory = loadFromCSV("saves/pareto.csv");
		System.out.println("Loaded " + utilHistory.size() + " contracts from file");

		if (utilHistory.size() == 0) {
			System.out.println("No contracts found. Starting from [1,2,3,...,197,198,199]");
			int[] startingContract = java.util.stream.IntStream.range(0, 200).toArray();
			utilHistory.add(new UtilContract(agA.getUtility(startingContract), agB.getUtility(startingContract), startingContract, 1));
		}

		scoreHistory = new ArrayList<>();
		for (UtilContract uc : utilHistory) {
			double[] scores = getBinaryScores(uc.getContract());
			scoreHistory.add(new ScoreContract(scores[1], scores[3], uc.getContract(), uc.getExplored()));
		}

		if (useScoring) {
			System.out.println("Using scoring method (slower)");
			runNegotiationScoring();
		} else {
			System.out.println("Using util method (faster)");
			runNegotiationUtil();
		}
	}

	public static void runNegotiationUtil() {

		for (int round = 1; round < numberOfRounds; round++) {

			UtilContract utilContract = utilHistory.get(0);
			for (UtilContract c : utilHistory) {
				if (c.getExplored() < utilContract.getExplored()) {
					utilContract = c;
				}
			}
			resetIndex();

			System.out.println(round + " CONTRACT " + utilContract.getUtilA() + " + " + utilContract.getUtilB() + " | EXPLORATION "  + utilContract.getExplored());
			
			while (i1 != utilContract.getContract().length) {
				int proposal[] = null;
				int exploration = utilContract.getExplored();
				if (exploration == 1) {
					proposal = constructNextProposal(utilContract.getContract());
				}
				else if (exploration == 2) {
					proposal = constructNextProposal3(utilContract.getContract());
				}
				else {
					System.out.println("Reached exploration " + exploration);
					break;
				}
				assert proposal != null;
				int utilA = agA.getUtility(proposal);
				int utilB = agB.getUtility(proposal);
				
				UtilContract newUtilContract = new UtilContract(utilA, utilB, proposal, 1);
				

				if (isParetoEfficientUtil(newUtilContract)) {
					System.out.print("NEW CONTRACT " + utilA + " + " + utilB + " = " + (utilA + utilB) + " [" + utilHistory.size() + "]");
					saveContract(saveFile, proposal, utilA, utilB);
					utilHistory.add(newUtilContract);
					utilHistory = removeNonParetoUtil();
					

					if (isParetoEfficientOrSameUtil(utilContract)) {
						System.out.println(". Aborting");
						break;
					}
					else {
						System.out.println(". Continuing...");
					}
				}
	
				if (logging) writeString(logFile, "Created new proposal:" + getStringFromArray(proposal));
				if (logging) logExactScores(logFile, agA.getScore(proposal), agB.getScore(proposal));
				
			}

			if (utilHistory.indexOf(utilContract) != -1) utilHistory.get(utilHistory.indexOf(utilContract)).setExplored(utilContract.getExplored() + 1);

		}

		System.out.println("DONE");
	}

	public static void runNegotiationScoring() {

		for (int round = 1; round < numberOfRounds; round++) {

			ScoreContract scoreContract = scoreHistory.get(0);
			for (ScoreContract c : scoreHistory) {
				if (c.getExplored() < scoreContract.getExplored()) {
					scoreContract = c;
				}
			}
			resetIndex();

			System.out.println(round + " CONTRACT " + scoreContract.getScoreA() + " + " + scoreContract.getScoreB() + " | EXPLORATION "  + scoreContract.getExplored());
			
			while (i1 != scoreContract.getContract().length) {
				int proposal[] = null;
				int exploration = scoreContract.getExplored();
				if (exploration == 0) {
					System.out.println("Found exploration 0");
					break;
				}
				else if (exploration == 1) {
					proposal = constructNextProposal(scoreContract.getContract());
				}
				else if (exploration == 2) {
					proposal = constructNextProposal3(scoreContract.getContract());
				}
				else {
					System.out.println("Reached exploration " + exploration);
					break;
				}
				assert proposal != null;
				
				double scores[] = getBinaryScores(proposal);
				
				
				ScoreContract newScoreContract = new ScoreContract(scores[1], scores[3], proposal, 1);
				

				if (isParetoEfficientScore(newScoreContract)) {
					System.out.print("NEW CONTRACT " + scores[1] + " + " + scores[3] + " = " + (scores[1] + scores[3]) + " [" + scoreHistory.size() + "]");

					saveContract(saveFile, proposal, agA.getUtility(proposal), agB.getUtility(proposal));
					scoreHistory.add(newScoreContract);
					scoreHistory = removeNonParetoScore();
					

					if (scoreHistory.indexOf(scoreContract) == -1) {
						System.out.println(". Aborting old contract"); // contract no longer pareto-efficient
						break;
					}
					else {
						System.out.println(". Continuing old contract");
					}
				}
	
				if (logging) writeString(logFile, "Created new proposal:" + getStringFromArray(proposal));
				if (logging) logExactScores(logFile, agA.getScore(proposal), agB.getScore(proposal));
				
			}
			
			if (scoreHistory.indexOf(scoreContract) != -1) scoreHistory.get(scoreHistory.indexOf(scoreContract)).setExplored(scoreContract.getExplored() + 1);

		}

		System.out.println("DONE");
	}

	public static void saveHistory(String saveFile) {
		for (UtilContract contract : utilHistory) {
			saveContract(saveFile, contract.getContract(), contract.getUtilA(), contract.getUtilB());
		}
	}

	public static double[] getBinaryScores(int[] proposal) {
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
		while (round < voteAccuracy) {

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
			round += 1;

			scores = new double[] { minScoreA, maxScoreA, minScoreB, maxScoreB };

		}

		return scores;
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

	public static boolean isParetoEfficientUtil(UtilContract newElement) {
        for (UtilContract element : utilHistory) {
            if (newElement.getUtilA() >= element.getUtilA() && newElement.getUtilB() >= element.getUtilB()) {
                return false; 
            }
        }
        return true;
    }

	public static boolean isParetoEfficientOrSameUtil(UtilContract newElement) {
        for (UtilContract element : utilHistory) {
            if (newElement.getUtilA() > element.getUtilA() && newElement.getUtilB() > element.getUtilB()) {
                return false; 
            }
        }
        return true;
    }

	public static List<UtilContract> removeNonParetoUtil() {
        List<UtilContract> newParetoElements = new ArrayList<>();

        for (UtilContract element : utilHistory) {
            boolean isPareto = true;
            for (UtilContract otherElement : utilHistory) {
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

	public static boolean isParetoEfficientScore(ScoreContract newElement) {
        for (ScoreContract element : scoreHistory) {
            if (newElement.getScoreA() >= element.getScoreA() && newElement.getScoreB() >= element.getScoreB()) {
                return false; 
            }
        }
        return true;
    }

	public static boolean isParetoEfficientOrSameScore(ScoreContract newElement) {
        for (ScoreContract element : scoreHistory) {
            if (newElement.getScoreA() > element.getScoreA() && newElement.getScoreB() > element.getScoreB()) {
                return false; 
            }
        }
        return true;
    }

	public static List<ScoreContract> removeNonParetoScore() {
        List<ScoreContract> newParetoElements = new ArrayList<>();

        for (ScoreContract element : scoreHistory) {
            boolean isPareto = true;
            for (ScoreContract otherElement : scoreHistory) {
				if (element == otherElement) continue;
                if (element.getScoreA() >= otherElement.getScoreA() && element.getScoreB() >= otherElement.getScoreB()) {
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
                    int[] contract = getArrayFromString(parts[3]);
                    contractList.add(new UtilContract(utilA, utilB, contract, 1)); // Set explored to 1
                } else {
                    System.out.println("Invalid line: " + line);
                }
            }
        } catch (IOException e1) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				writer.write("utilA;utilB;utilSum;contract;pareto");
				writer.newLine();
				System.out.println("CSV file created successfully.");
				loadFromCSV(filePath);
			} catch (IOException e2) {
				System.err.println("Error writing to CSV file: " + e2.getMessage());
			}
        }

        return contractList;
    }
}