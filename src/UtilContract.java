public class UtilContract {
    private int utilA;
    private int utilB;
    private int utilSum;
    private int[] contract;
    private int explored;

    public UtilContract(int utilA, int utilB, int utilSum, int[] contract, int explored) {
        this.utilA = utilA;
        this.utilB = utilB;
        this.utilSum = utilSum;
        this.contract = contract;
        this.explored = explored;
    }

    public UtilContract(int utilA, int utilB, int utilSum, int[] contract) {
        this.utilA = utilA;
        this.utilB = utilB;
        this.utilSum = utilSum;
        this.contract = contract;
        this.explored = 1;
    }



    public int getUtilA() {
        return utilA;
    }

    public void setUtilA(int utilA) {
        this.utilA = utilA;
    }

    public int getUtilB() {
        return utilB;
    }

    public void setUtilB(int utilB) {
        this.utilB = utilB;
    }

    public int getUtilSum() {
        return utilSum;
    }

    public void setUtilSum(int utilSum) {
        this.utilSum = utilSum;
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