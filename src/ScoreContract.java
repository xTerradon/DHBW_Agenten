public class ScoreContract {
    private double scoreA;
    private double scoreB;
    private int[] contract;
    private int explored;
    
    // Constructor
    public ScoreContract(double scoreA, double scoreB, int[] contract, int explored) {
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.contract = contract;
        this.explored = explored;
    }
    
    // Getters and Setters
    public double getScoreA() {
        return scoreA;
    }
    
    public void setScoreA(double scoreA) {
        this.scoreA = scoreA;
    }
    
    public double getScoreB() {
        return scoreB;
    }
    
    public void setScoreB(double scoreB) {
        this.scoreB = scoreB;
    }
    
    public int[] getContract() {
        return contract;
    }

    public void setContract(int[] contract) {
        this.contract = contract;
    }

    public int getExplored() {
        return explored;
    }

    public void setExplored(int explored) {
        this.explored = explored;
    }
}
