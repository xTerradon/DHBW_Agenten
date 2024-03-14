import java.io.File;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class SupplierAgent extends Agent {

	private double minCost;
	private double maxCost;

	private int[] lastProposal;
	private int lastProposalRound;

	private int[][] costMatrix;

	public SupplierAgent(File file, int minCost, int maxCost) throws FileNotFoundException {

		Scanner scanner = new Scanner(file);
		int dim = scanner.nextInt();
		costMatrix = new int[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				int x = scanner.nextInt();
				costMatrix[i][j] = x;
			}
		}
		scanner.close();

		this.minCost = (double) minCost;
		this.maxCost = (double) maxCost;

		this.lastProposal = new int[costMatrix.length];
		this.lastProposalRound = 0;
	}

	public boolean voteScore(int[] proposal) {
		double val = getScore(proposal);
		double randomNumber = Math.random();

		return (randomNumber < val);
	}

	public double getScore(int[] proposal) {
		int costProposal = evaluate(proposal);

		double val = ((double) costProposal - minCost) / (maxCost - minCost);
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
		} else {
			lastProposalRound += 1;
		}

		double score = getScore(proposal);
		return getValueAtIteration(score, lastProposalRound - 1);
	}

	public int getContractSize() {
		return costMatrix.length;
	}

	public void printUtility(int[] contract) {
		System.out.print(evaluate(contract));
	}

	public int getUtility(int[] contract) {
		return evaluate(contract);
	}

	private int evaluate(int[] contract) {

		int result = 0;
		for (int i = 0; i < contract.length - 1; i++) {
			int zeile = contract[i];
			int spalte = contract[i + 1];
			result += costMatrix[zeile][spalte];
		}

		return result;
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