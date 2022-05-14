import org.apache.commons.math3.util;
import java.util;
import Panel;
import Utils;
import BigInteger;
import ArrayList;
import HashSet;
import java.util.Set;
import CombinatoricsUtils;
import Arrays;

stop()

# For each pair of players x and y, x D y or y D x
# With n = 5 players, there are C(5,2) = 5! / (2! * 3!) = 120 / (2 * 6)
# So there are C(5,2) = 120 / 12 = 10 pairs of players.
# For each pair, the player that DOMINATES can be chosen in 2 ways
# so that there are 20 different binary relations in the graph
# CHECK:
# (1,2), (1,3), (1,4), (1,5)
# (2,3), (2,4), (2,5)
# (3,4), (3,5)
# (4,5)
# 
# If all 20 binary relations are defined, the the network is complete. How do I
# generate all such complete networks? 

public static void generate(int n, int r) {

    Iterator<int[]> iterator = combinationsIterator(n, r);       
    while (iterator.hasNext()) {
        final int[] combination = iterator.next();
        System.out.println(Arrays.toString(combination));
    }
}

generate(5,2)
stop()

public class AnalyzeOutcomeData implements IGetSimulationResults {

    private long masterSeed;
    private int bSize;
    private int jSize;
    private int nDimensions;
    private double[][] y;
    private int nNegate = 0;
    private int nNeuter = 0;
    private Panel pool;
    private int method;
    private double[] pUnfavorable;
    private long customerId;
    private Set<String> favorableResults;
    private CompRowMatrix modelMatrix;
    private double[][] statistics;
    private Utils utils = new Utils();

    public AnalyzeOutcomeData() {

        int groupSize = 4;

        int nComb = utils.countCombinations(groupSize, 2);



        utils.buildMatrix(nComb.intValue(), groupSize);
        ArrayList<Integer[]> data = utils.getMatrix();

        double[][] model = new double[data.size()][bSize];
        Integer[] row = new Integer[bSize];

        for (int count = 0; count < data.size(); count++) {
            row = data.get(count);
            for (int i = 0; i < bSize; i++) {
                model[count][i] = row[i].intValue();
            }

        }
        modelMatrix = new CompRowMatrix(new DenseMatrix(model));
    }

    public long getMasterSeed() {
        return masterSeed;
    }

    /**
     * 
     * @param val Assigned to masterSeed
     */
    public void setMasterSeed(final long val) {
        masterSeed = val;
    }

    public int getMethod() {
        return method;
    }

    public int getNDimensions() {
        return nDimensions;
    }

    public int getNNegate() {
        return nNegate;
    }

    public int getNNeuter() {
        return nNeuter;
    }

    public Panel getPool() {
        return pool;
    }

    public double[][] getY() {
        return y;
    }

    public void setJurySize(final int val) {
        jSize = val;
    }

    public void setMethod(final int in) {
        method = in;
    }

    public void setNDimensions(final int val) {
        nDimensions = val;
    }

    public void setpool(final Panel in) {
        pool = in;
    }

    public CompRowMatrix getModelMatrix() {
        return modelMatrix;
    }

    public int getBLOCK_LIMIT() {
        return BLOCK_LIMIT;
    }

    /**
     * @param val the value assigned to xCRM
     */
    public void setModelMatrix(CompRowMatrix val) {
        modelMatrix = val;
    }

    /**
     * @param row indexes the model row
     * @param result the array of 0/1 results from BLOCK_LIMIT simulations
     */
    public void updateY(int row, int[] result) {
        for (int i = 0; i < y[0].length; i++) {
            this.y[row][i] = result[i];
        }
    }

    /**
     * @return the pUnfavorable
     */
    public double[] getPUnfavorable() {
        return pUnfavorable;
    }

    /**
     * @param val the value assigned to pUnfavorable
     */
    public void setPUnfavorable(double[] val) {
        this.pUnfavorable = val;
    }

    /**
     * @return the runId
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     * @param val the value assigned to runId
     */
    public void setCustomerId(long val) {
        customerId = val;
    }

    /**
     * @return the favorableResults
     */
    public Set getFavorableResults() {
        return favorableResults;
    }

    /**
     * @param target Used to define the favorableResults set
     * @return 
     */
    public boolean setFavorableResults(IGetSimulationResults.Goal target) {

        boolean ok = TRUE;

        favorableResults = new HashSet<String>();
        switch (target) {
            case CONVICT:
                ok = favorableResults.add(String.valueOf(-1));
                break;
            case ACQUIT:
                ok = favorableResults.add(String.valueOf(1));
                ok = ok && favorableResults.add(String.valueOf(0));
                break;
            case CONVICT_OR_HUNG:
                ok = favorableResults.add(String.valueOf(-1));
                ok = ok && favorableResults.add(String.valueOf(0));
                break;
            case ACQUIT_OR_HUNG:
                ok = favorableResults.add(String.valueOf(1));
                ok = ok && favorableResults.add(String.valueOf(0));
                break;
            default:
                System.out.println("GenOutcomeData.setFavorableResults: unknown val of target");
        }
        return ok;
    }

    /**
     * @return the statistics
     */
    public double[][] getStatistics() {
        return statistics;
    }
}
