import java.io.*;
import java.util.Arrays;
import java.util.Random;


public class Mediator {

	int contractSize;
	
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

	public int[] constructProposal(int[] contract) {
		int[] proposal = new int[contractSize];
		for(int i=0;i<proposal.length;i++)proposal[i] = contract[i];
		int element = (int)((proposal.length-1)*Math.random());
		int wert1   = proposal[element];
		int wert2   = proposal[element+1];
		proposal[element]   = wert2;
		proposal[element+1] = wert1;
		return proposal;
	}
	
	public int[] constructSwitchedProposal(int[] contract) {
		int[] proposal = new int[contractSize];
		for(int i=0;i<proposal.length;i++)proposal[i] = contract[i];
		int element1 = (int)((proposal.length-1)*Math.random());
		int element2 = (int)((proposal.length-1)*Math.random());
		int wert1   = proposal[element1];
		int wert2   = proposal[element2];
		proposal[element1]   = wert2;
		proposal[element2] = wert1;
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

}
