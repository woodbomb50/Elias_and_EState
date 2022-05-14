import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import no.uib.cipr.matrix.Matrix;

/**
 * Use outcomes database to evaluate panelist
 *
 * Assumption(s):
 *  1) The simulator MUST be run prior to the trial in order to have an
 *     initial evaluation of those jurors initially placed in the jury box.
 *  2) The belief estimates for each panelist in the jury box can be updated
 *     at any time.
 *  3) The demographics for eaxch panelist can be updated at any time.
 *  4) Whenever a new juror is brought into the jury box, the simulator will be
 *     run based on the status of the panelists remaining in the jury box.
 *  5) It si assumed that whenever a new panelist is brought in, they become
 *     the last panelist to be examined.
 *
 * The current state of the panel is provided and the system recommends whether
 * the current panelist should be stricken.
 * 1)
 * @author Roy Wilson
 */
public class EvaluateOutcomeData {

    private long cId;
    private Matrix x0;
    private Matrix y0;
    // private Set<Integer> sourceList = new HashSet<Integer>();
    // private ArrayList<Double[]> pvals = new ArrayList<Double[]>();
    private String xfileName;
    private String yfileName;
    private String rfileName;
    private int nXRows;
    private int nXCols;
    private int nYCols;
    private int focus;
    private double[][] statistics;

    /**
     *
     */
    public void createDataForR() {

        try {
            BufferedWriter xOut = new BufferedWriter(new FileWriter(xfileName));
            BufferedWriter yOut = new BufferedWriter(new FileWriter(yfileName));

            for (int j = focus; j < nXCols; j++) {
                xOut.write("\"P" + (j + 1) + "\" ");
            }

            xOut.write("\n");
            for (int i = 0; i < nXRows; i++) {
                for (int j = focus; j < nXCols; j++) {
                    if (x0.get(i, j) == 0) {
                        xOut.write("0 ");
                    } else {
                        xOut.write("1 ");
                    }
                }
                for (int j = 0; j < nYCols; j++) {
                    yOut.write(y0.get(i, j) + " ");
                }
                xOut.write("\n");
                yOut.write("\n");
            }
            xOut.close();
            yOut.close();
        } catch (IOException e) {
            System.out.println("EvaluateOutcomeData: createDataForR:" + e);
            System.exit(1);
        }
    }

    public final void runRprogram() throws Exception {

        String[] line = {
            "x <- read.table(" + "\"" + xfileName + "\",header=T)",
            "y <- read.table(" + "\"" + yfileName + "\",header=F)",
            "s <-apply(y,1,sum)",
            "library(brglm)",
            "mod.brglm <- brglm(cbind(s,(10 - s)) ~ .-1, family=binomial,data=x)",
            "summary(mod.brglm)",
            "q(save=" + "\"" + "yes" + "\"" + ",status=0,runLast=F)"
        };

        try {
            BufferedWriter rOut = new BufferedWriter(new FileWriter(rfileName));

            for (int i = 0; i < line.length; i++) {
                rOut.write(line[i] + "\n");
            }
            rOut.close();
        } catch (IOException e) {
            System.out.println("EvaluateOutcomeData: runRProgram:" + e);
            System.exit(1);
        }
        Runtime rt = Runtime.getRuntime();
        String[] cmd = new String[4];
        cmd[0] = "/usr/bin/R";
        cmd[1] = "--vanilla";
        cmd[2] = "-f";
        cmd[3] = rfileName;
        Process p = rt.exec(cmd);
        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader r = new BufferedReader(isr);
        String x;
        ArrayList<String> output = new ArrayList<String>();

        while ((x = r.readLine()) != null) {
            output.add(x);
        }

        r.close();
        p.waitFor();
        //
        // FIX this
        //
        int nPersons = 14;
        ArrayList<String> filtered = new ArrayList<String>();
        for (int i = 33; i < 33 + (nPersons-focus); i++) {
            filtered.add(output.get(i));
        }
        System.out.println("entering idemtify dangers");
        identifyDangers(filtered);
        System.out.println("bak from id dangers");
    }

    private void identifyDangers(ArrayList<String> data) {

        int nPersons = data.size();
        System.out.println("nPersons => " + nPersons);
        StringTokenizer parser;
        statistics = new double[nPersons][3];
        String line;

        for (int i = 0; i < nPersons; i++) {
            line = data.get(i);
            System.out.println("line " + i + ": " + line);
            parser = new StringTokenizer(line);
            final int nTokens = parser.countTokens();
            String[] splitLine = new String[nTokens];
            System.out.println("getting tokens");
            for (int j = 0; j < nTokens; j++) {
                splitLine[j] = parser.nextToken();
            }
            System.out.println("splitting line 1");
            statistics[i][0] = Double.parseDouble(splitLine[1]);
            System.out.println("splitting line 3");
            statistics[i][1] = Double.parseDouble(splitLine[3]);
            System.out.println("splitting line 4");
            statistics[i][2] = Double.parseDouble(splitLine[4]);
        }
    }

    /**
     *
     * @param customerId
     * @param focus
     * @param x
     * @param y
     * @throws Exception 
     */
    public EvaluateOutcomeData(
            final int focus,
            final Matrix x,
            final Matrix y) throws Exception {

        this.focus = focus;
        x0 = x;
        y0 = y;

        Set<Integer> currentState = new LinkedHashSet<Integer>();

        int nPersons = x.numColumns();
        for (int i = 0; i < nPersons; i++) {
            currentState.add(Integer.valueOf(i));
        }

        xfileName = "c" + String.valueOf(cId) + "X.dat";
        yfileName = "c" + String.valueOf(cId) + "Y.dat";
        rfileName = "c" + String.valueOf(cId) + ".R";
        nXRows = x.numRows();
        nXCols = x.numColumns();
        nYCols = y.numColumns();
        createDataForR();
        System.out.println("about to runRprogram");
        runRprogram();
        System.out.println("runRprogram complete");
    }

    /**
     * @return the statistics
     */
    public double[][] getStatistics() {
        return statistics;
    }
}
