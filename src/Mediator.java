import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Mediator {

	int i1 = 0;
	int i2 = 1;

	int contractSize;
	private List<int[]> contractHistory = new ArrayList<>();

	public Mediator(int contractSizeA, int contractSizeB) throws FileNotFoundException {
		if (contractSizeA != contractSizeB) {
			throw new FileNotFoundException(
					"Verhandlung kann nicht durchgefuehrt werden, da Problemdaten nicht kompatibel");
		}
		this.contractSize = contractSizeA;
	}

	public int[] initContract() {
		int[] contract = new int[contractSize];
		for (int i = 0; i < contractSize; i++)
			contract[i] = i;
		return contract;
	}

	public void resetIndex() {
		i1 = 0;
		i2 = 1;
	}

	public int[] constructNextProposal(int[] contract) {
		int[] proposal = new int[contract.length];
		for (int i = 0; i < proposal.length; i++)
			proposal[i] = contract[i];

		int temp = proposal[i1];
		proposal[i1] = proposal[i2];
		proposal[i2] = temp;

		i2++;
		if (i2 == proposal.length) {
			i1++;
			i2 = Math.min((i1+1), proposal.length-1);
		}
		if (i1 == proposal.length) {
			System.exit(0);
		}

		return proposal;
	}

	public int[] constructTwoSwitchedProposal(int[] contract) {
		int[] proposal = new int[contract.length];
		for (int i = 0; i < proposal.length; i++)
			proposal[i] = contract[i];

		int element1 = (int) (proposal.length * Math.random());
		int element2;
		do {
			element2 = (int) (proposal.length * Math.random());
		} while (element1 == element2);

		int temp = proposal[element1];
		proposal[element1] = proposal[element2];
		proposal[element2] = temp;

		return proposal;
	}

	public int[] constructThreeSwitchedProposal(int[] contract) {
		int[] proposal = new int[contract.length];
		for (int i = 0; i < proposal.length; i++)
			proposal[i] = contract[i];

		int element1 = (int) (proposal.length * Math.random());
		int element2;
		do {
			element2 = (int) (proposal.length * Math.random());
		} while (element1 == element2);
		int element3;
		do {
			element3 = (int) (proposal.length * Math.random());
		} while (element3 == element1 || element3 == element2);

		int temp1 = proposal[element1];
		int temp2 = proposal[element2];
		proposal[element1] = proposal[element2];
		proposal[element2] = temp1;
		proposal[element3] = temp2;
		return proposal;
	}

	public int[] constructRandomProposal(int[] contract) {
		int[] shuffledContract = Arrays.copyOf(contract, contract.length);
		shuffleArray(shuffledContract);
		return shuffledContract;
	}

	private static void shuffleArray(int[] array) {
		Random rand = new Random();

		for (int i = array.length - 1; i > 0; i--) {
			int index = rand.nextInt(i + 1);

			// Swap array[i] with array[index]
			int temp = array[i];
			array[i] = array[index];
			array[index] = temp;
		}
	}

	public int[] getUniqueProposal(int[] contract, String proposalCreation) {
		assert proposalCreation.equals("random") || proposalCreation.equals("2") || proposalCreation.equals("3")
				|| proposalCreation.equals("next");

		int[] proposal = null;
		if (proposalCreation.equals("random")) {
			proposal = constructRandomProposal(contract);
		} else if (proposalCreation.equals("2")) {
			proposal = constructTwoSwitchedProposal(contract);
		} else if (proposalCreation.equals("3")) {
			proposal = constructThreeSwitchedProposal(contract);
		} else if (proposalCreation.equals("next")) {
			proposal = constructNextProposal(contract);
			contractHistory.add(proposal);
			return proposal;
		}

		if (checkExistence(proposal)) {
			proposal = getUniqueProposal(contract, proposalCreation);
		}

		contractHistory.add(proposal);
		return proposal;
	}

	private boolean checkExistence(int[] proposal) {
		for (int[] existingContract : contractHistory) {
			if (Arrays.equals(existingContract, proposal)) {
				return true;
			}
		}
		return false;
	}
}
