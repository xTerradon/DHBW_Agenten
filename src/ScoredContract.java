
public class ScoredContract {
	public int[] contract;
    public double[] scores;

    public ScoredContract(int[] contract, double[] scores) {
        this.contract = contract;
        this.scores = scores;
    }
    
    public int[] getContract() {
    	return contract;
    }
    
    public double[] getScores() {
    	return scores;
    }
    
    public void printScoredContract() {
		System.out.println(
				"A: [ " + String.format("%.4f", scores[0]) + " > " + String.format("%.4f", scores[1]) + " ]" + 
				" B: [ " + String.format("%.4f", scores[2]) + " > " + String.format("%.4f", scores[3]) + " ]"
		);
    }
    
    public String getString() {
    	String str = "A: [ " + String.format("%.4f", scores[0]) + " > " + String.format("%.4f", scores[1]) + " ]" + 
    				 " B: [ " + String.format("%.4f", scores[2]) + " > " + String.format("%.4f", scores[3]) + " ]";
    	return str;
    }
}
