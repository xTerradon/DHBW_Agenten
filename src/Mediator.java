import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Mediator {

	int contractSize;
	private List<int[]> contractHistory = new ArrayList<>();
	
	public Mediator(int contractSizeA, int contractSizeB) throws FileNotFoundException{
		if(contractSizeA != contractSizeB){
			throw new FileNotFoundException("Verhandlung kann nicht durchgefuehrt werden, da Problemdaten nicht kompatibel");
		}
		this.contractSize = contractSizeA;
	}
	
	public int[] initContract(){
		int[] contract = new int[contractSize];
		for(int i=0;i<contractSize;i++)contract[i] = i;
		return contract;
	}
	
	public int[] constructSwitchedProposal(int[] contract) {
		int[] proposal = new int[contract.length];
		for(int i=0; i<proposal.length; i++) proposal[i] = contract[i];
		
		int element1 = (int)(proposal.length*Math.random());
		int element2;
		do {
			element2 = (int)(proposal.length*Math.random());
		} while (element1 == element2);
		
		int temp = proposal[element1];
		proposal[element1] = proposal[element2];
		proposal[element2] = temp;
		
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
		int[] proposal = null;
		if (proposalCreation.equals("random")) {
			proposal = constructRandomProposal(contract);
		} else if (proposalCreation.equals("switched")) {
			proposal =  constructSwitchedProposal(contract);
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
