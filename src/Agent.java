public abstract class Agent {

	public abstract boolean	vote(int[] contract, int[] proposal);
	public abstract boolean voteScore(int[] proposal);
	public abstract boolean voteScoreBinary(int[] proposal);
	public abstract boolean getValueAtIteration(double value, int targetIteration);
	public abstract double	getScore(int[] proposal);
	public abstract void    printUtility(int[] contract);
	public abstract int		getUtility(int[] contract);
	public abstract int     getContractSize();
}
