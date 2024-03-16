public class UtilContract {
    private int utilA;
    private int utilB;
    private int[] contract;
    private int explored;

    public UtilContract(int utilA, int utilB, int[] contract, int explored) {
        this.utilA = utilA;
        this.utilB = utilB;
        this.contract = contract;
        this.explored = explored;
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