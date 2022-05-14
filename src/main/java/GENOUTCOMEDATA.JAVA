import com.jsr.selection.Panel;
import com.jsr.selection.BIBD;

import com.jsr.selection.JuryParameters;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.One2OneChannelInt;
import org.jcsp.lang.Parallel;

import com.jsr.selection.PanelData;
import com.jsr.selection.Utils;
import java.math.BigInteger;

import org.apache.log4j.Logger;

/**
 * The predefined model matrix is used to drive the simulator BLOCK_LIMIT times
 * for each row in the matrix.
 *
 * Each run uses the parameter values defined for one BLOCK_LIMIT block.
 *
 * The value assigned to y, the outcome variable, is the sum of the
 * values assigned (0 or 1) to the outcome for each block.
 * @author Roy Wilson
 */
public class GenOutcomeData implements
        IGetSimulationResults,
        CSProcess {

    final static int time = 5400;
    final static int interactions = (int) (time / 3600.0 * 1336);
    static double[][] boundary;
    /* WHAT IS LU? */
    final static double[][] xLU = {{0.00, 0.25}, {0.25, 0.50}, {0.50, 0.75},
        {0.75, 1.00}
    };
    final static double[][] yLU = {{0.00, 0.25}, {0.25, 0.50}, {0.50, 0.75},
        {0.75, 1.00}
    };
    private long masterSeed;
    private SimulateOneMeeting[] mtg = new SimulateOneMeeting[BLOCK_LIMIT];
    private int bSize;
    private int jSize;
    private int nDimensions;
    private double[][] y;
    private double decisionThreshold = 0.50; // rational actor assumption
    private Panel pool;
    private int method;
    private double[] testBelief;
    private double[] pUnfavorable;
    private long customerId;
    private Set<String> favorableResults;
    private CompRowMatrix modelMatrix;
    private Controller controller = new Controller();
    private Harvester harvester = new Harvester();
    private One2OneChannel harvester2controller = Channel.one2one();
    private One2OneChannelInt[] workers2harvester = Channel.one2oneIntArray(BLOCK_LIMIT);
    private PanelData panelData;
    private double[][] statistics;
    private Parallel par;
    final static Logger logger = Logger.getLogger(com.jsr.deliberation.SimulateOneMeeting.class);
    private Utils utils = new Utils();


    public GenOutcomeData(int size) {

        groupSize = size;
    }

    public double[][] generateFullMatrix() {

        BigInteger nComb = utils.countCombinations(bSize, jSize);

        int combCount = nComb.intValue();

        utils.buildModel(combCount, bSize, jSize);
        double[][] m = utils.getModel();
        modelMatrix = new CompRowMatrix(new DenseMatrix(m));
        return m;
    }

    /**
     * @param v Number of panelists
     * @param k Jury size
     */
    public void generatePartMatrix(int v, int k) {

        BIBD design = new BIBD(v, k);
        int nBlocks = design.getB();
        if (k == 1) {
            nBlocks = 21;
        }
        double[][] model = new double[nBlocks][bSize];
        String curDir = System.getProperty("user.dir");
        String fileName = "/jsr-catalog-" + v + "." + k + ".dat";
        String filePath = curDir + fileName;

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader in = new BufferedReader(fileReader);
            String line = "null";
            String tmpStr = null;

            for (int count = 0; count < nBlocks; count++) {
                if ((tmpStr = in.readLine()) != null) {
                    line = tmpStr.trim();
                    String[] temp = line.split(" ");
                    for (int i = 0; i < bSize; i++) {
                        model[count][i] = Integer.parseInt(temp[i]);
                    }
                } else {
                    break;
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
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

    private void genOutcomes(int row) {

        par = new Parallel(new CSProcess[]{
                    new Parallel(mtg),
                    harvester,
                    controller});
        par.run();
        par.releaseAllThreads();

        y[row] = controller.getOutcomes();
    }

    @Override
    public void run() {

        // panelData = new PanelData(customerId, bSize, nDim, Lthresh, Rthresh)

        controller.setIn(harvester2controller);
        harvester.setFavoredOutcomes(favorableResults);
        harvester.setIn(workers2harvester);
        harvester.setOut(harvester2controller);
        harvester.setNWorkers(BLOCK_LIMIT);

        for (int bid = 0; bid < BLOCK_LIMIT; bid++) {
            // System.out.println(" bid => " + bid);
            mtg[bid] = new SimulateOneMeeting();
            mtg[bid].setLogger(logger);
            mtg[bid].setDecisionRule(getDecisionRule());
            mtg[bid].setBid(bid);
            mtg[bid].setMsgLimit(interactions);
            mtg[bid].setGroupSize(jSize);
            mtg[bid].setDecisionThreshold(decisionThreshold);
            mtg[bid].allocateStorage();
            mtg[bid].setChan(workers2harvester[bid]);
        }
        // Assume belief convergence occurs within 90 minutes or not all all
        testBelief = new double[jSize];
        int rowCount = modelMatrix.numRows();
        // System.out.println("rowCount => " + rowCount + " jSize => " + jSize);
        y = new double[rowCount][BLOCK_LIMIT];
        Set<Integer> candidateList = new HashSet<Integer>();
        int Lthresh = 28;
        int Rthresh = 56;

        for (int k = 0; k < 1; k++) {
            long seed = (k+1) * System.currentTimeMillis();
            Random random = new Random(seed);
            double Dthresh = 0.50;

            ## JuryParameters jp =
            ##        new JuryParameters(iMeth, cMeth, bSize, jSize, nDimensions, Lthresh, Rthresh, Dthresh, random);

            int code;
            for (int row = 0; row < rowCount; row++) {
                candidateList.clear();
                for (int col = 0; col < modelMatrix.numColumns(); col++) {
                    code = (int) modelMatrix.get(row, col);
                    if (code == 1) {
                        candidateList.add(Integer.valueOf(col));
                    }
                }
                panelData.gatherJuryData(candidateList);

                EncodedJuryData ejd = new EncodedJuryData(jp, panelData.getJuryData());
                Integer[] temp = ejd.getBeliefData();

                for (int i = 0; i < jSize; i++) {
                    testBelief[i] = temp[i] / 100.0;
                }

                for (int bid = 0; bid < BLOCK_LIMIT; bid++) {
                    mtg[bid].setStatusData(ejd.getStatusData());
                    mtg[bid].setup(block[bid], testBelief);
                }
                genOutcomes(row);
            }
        }
    }

    /*
     * Generate a random sample of eta, pi, theta within bounds
     */
    private double[] createSamplePoint(final double[] bound, Random[] rx) {

        // bound = {lowerX, upperX, lowerY, upperY }

        double temp = rx[0].nextDouble();
        while ((bound[0] > temp) || (temp > bound[1])) {
            temp = rx[0].nextDouble();
        }
        double[] point = {0.0, 0.0, 0.0};
        point[0] = temp;

        temp = rx[1].nextDouble();
        while ((bound[2] > temp) || (temp > bound[3])) {
            temp = rx[1].nextDouble();
        }
        point[1] = temp;

        temp = rx[2].nextDouble();
        while ((temp > point[1]) || (temp < bound[2])) {
            temp = rx[2].nextDouble();
        }
        point[2] = temp;
        return point;
    }

    public int getMethod() {
        return method;
    }

    public int getNDimensions() {
        return nDimensions;
    }

    public Panel getPool() {
        return pool;
    }

    public double[][] getY() {
        return y;
    }

    /*
     * Each of this.nBlocks blocks is defined by the values of 3 parameters:
     * eta, pi, and theta. The value of each is randomly selected based on
     * pre-defined block boundaries.
     **/
    public double[][] initializeBlocks() {

        double[] p;
        double[][] b = new double[getBLOCK_LIMIT()][3];
        long seed = System.currentTimeMillis();
        // System.out.println("initializeBlocks: seed => " + seed);
        Random randSeed = new Random(seed);
        Random[] rx = {new Random(randSeed.nextLong()),
            new Random(randSeed.nextLong()),
            new Random(randSeed.nextLong())
        };

        for (int i = 0; i < block.length; i++) {
            // System.out.print(i + " ");
            p = createSamplePoint(block[i], rx);
            for (int j = 0; j < 3; j++) {
                b[i][j] = p[j];
                // System.out.print(p[j] + " ");
            }
            // System.out.println();
        }
        return b;
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
     * @return the decisionRule
     */
    public int getDecisionRule() {
        return decisionRule;
    }

    /**
     * @param val Assigned to decisionRule
     */
    public void setDecisionRule(int val) {
        this.decisionRule = val;
    }

    /**
     * @return the statistics
     */
    public double[][] getStatistics() {
        return statistics;
    }
}
