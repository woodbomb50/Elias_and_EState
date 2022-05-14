import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/* Need log4j */
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.SimpleLayout;
 
public class SimulateOneMeeting implements ISimulateOneMeeting {

    private Random random;
    private int msgLimit; // MAXIMUM number of dyadic interactions
    private double eta; // probability that a tie forms based on status
    private double pi; // probability a tie forms based on initiator behavior
    private double theta; // probability that a tie forms based on observation
    // NOTE: eta + pi + theta =?= 1
    double[][] influence;
    private boolean consensus; // true if belief converges
    private int steps2consensus; // interactions until consensus
    private StatusValue.Values[][] statusData;
    private int nDimensions; // number of different statuses modeled
    private int groupSize = 5; // size of group
    private int nPathPairs[][]; // number of paths from actor x to outcomes
    private int[][] participationCount; // number of times x addresses y
    private double[][] pXaddressY; // probability that x addresses y
    private Ties ties; // set of dyadic ties
    private double consensusBelief; // belief of actors when consensus
    private int bid;
    private int decisionRule = UNANIMOUS;
    private boolean logging = false;

    public void allocateStorage() {

        setTies(new Ties(getGroupSize()));
        setPXaddressY(new double[getGroupSize()][getGroupSize()]);
        setParticipationCount(new int[getGroupSize()][getGroupSize()]);
        Binitial = new double[getGroupSize()];
        setBupdate(new double[getGroupSize()]);
        nPathPairs = new int[getGroupSize()][2];
    }

    private boolean bothTiesUndefined(final int a, final int b) {

        int Ustate = ISocialStructure.UNDEFINED;
        return ((getTies().getTieState(a, b) == Ustate) && (getTies().getTieState(b, a) == Ustate));
    }

    // the focal actor is the one addressed by another
    private double computePS(final int focal, final int other) {

        // e_factor comes from Fisek, Berger, and Norman, AJS Vol. 97 No 1 1991, p. 128
        double e_factor = 0.81737;
        int k = getPathPairs()[other][PATH_COUNT_POS];
        int j = getPathPairs()[other][PATH_COUNT_NEG];
        double aesOther = -Math.pow(e_factor, k) + Math.pow(e_factor, j);
        k = getPathPairs()[focal][PATH_COUNT_POS];
        j = getPathPairs()[focal][PATH_COUNT_NEG];
        double aesFocal = -Math.pow(e_factor, k) + Math.pow(e_factor, j);

        // coefficients in num are from Balkwell, ASR, Vol. 56, p. 359
        double num = Math.exp(0.472 + 0.632 * (aesOther - aesFocal));
        return num / (num + 1);
    }

    // Compute the number of pairs from x to task outcomes
    private void countPathsToOutcomes(final int x, final int y) {

        int vx, vy;

        for (int d = 0; d < nDimensions; d++) {
            vx = getStatusData()[x][d].ordinal();
            if (x != y) {
                vy = getStatusData()[y][d].ordinal();
                if (vy < vx) {
                    getPathPairs()[x][PATH_COUNT_POS]++;
                    getPathPairs()[y][PATH_COUNT_NEG]++;
                } else if (vx < vy) {
                    getPathPairs()[x][PATH_COUNT_NEG]++;
                    getPathPairs()[y][PATH_COUNT_POS]++;
                }
            }
        }
    }

    // In the case of a jury, P is "defendant is guilty"
    // "1" (affirmative wrt P)
    // "0" (undecided wrt P)
    // "-1" (negative wrt P)
    private int decideByVoting() {

        int affirmCount = 0;
        int decision = 0;
        int gSize = getGroupSize();
        for (int x = 0; x < gSize; x++) {
            if (getBupdate()[x] > decisionThreshold) {
                affirmCount += 1;
            }
        }
        switch (decisionRule) {
            case UNANIMOUS:
                if (affirmCount == gSize) {
                    decision = 1; // affirm P
                } else if (affirmCount == 0) {
                    decision = -1; // negate P
                } else {
                    decision = 0; // undecided regarding P
                }
                break;
            case MAJORITY:
                if (2 * affirmCount == gSize) {
                    decision = 0; // undecided regarding P
                } else if (2 * affirmCount < getGroupSize()) {
                    decision = -1; // negate P
                } else {
                    decision = 1; // affirm P
                }
                break;
            default:
                System.out.println("SimulateOneMeeting.decideByVoting: unknown decisionRule");
                System.out.println("jsrSystem is halting");
                System.exit(-1);
        }

        return decision;
    }

    /**
     * Taken from C program by John Skvoretz
     */
    private int[] getNextPair() {

        double variate = random.nextDouble(), lCut, rCut = 0.0;
        boolean done = false;
        int x = 0, y = 0;

        while (!done && (x < getGroupSize())) {
            y = 0;
            while (!done && (y < getGroupSize())) {
                if (y != x) {
                    lCut = rCut;
                    rCut += getPXaddressY()[x][y];
                    if ((lCut <= variate) && (variate < rCut)) {
                        done = true;
                    }
                }
                if (!done) {
                    y++;
                }
            }
            if (!done) {
                x++;
            }
        }
        // modified to randomly select actors in { 0, ..., groupSize-1 }
        // instead of picking the last actor as in Skvoretz's code
        if (getGroupSize() <= x) {
            x = random.nextInt(getGroupSize());
        }
        // x = groupSize-1;
        if (getGroupSize() <= y) {
            y = random.nextInt(getGroupSize());
            while (y == x) {
                y = random.nextInt(getGroupSize());
            }
            // y = groupSize - 1;
        }
        int[] pair = {x, y};
        return pair;
    }

    private int getParticipationCount(final int x) {

        int count = 0;
        for (int y = 0; y < getGroupSize(); y++) {
            if (y != x) {
                count += getParticipationCount()[x][y];
            }
        }
        return count;
    }

    public int[] getParticipationCounts() {

        int[] result = new int[getGroupSize()];
        for (int x = 0; x < getGroupSize(); x++) {
            result[x] = getParticipationCount(x);
        }
        return result;
    }

    public int getSteps2consensus() {
        return steps2consensus;
    }

    private void handleBystanders(final int x, final int y, final int sc) {

        StatusValue.Values xStatus;
        StatusValue.Values yStatus;
        StatusValue.Values zStatus;

        for (int z = 0; z < getGroupSize(); z++) {
            if ((z == x) || (z == y)) {
                continue;
            }
            // get value of the scth status characteristic
            xStatus = getStatusData()[x][sc];
            yStatus = getStatusData()[y][sc];
            zStatus = getStatusData()[z][sc];

            /* equations (4), (6) pp. 1379-80 of AJS */
            int tieState = getTies().getTieState(x, y);

            if ((tieState == ISocialStructure.DOMINATES) || (tieState == ISocialStructure.UNDEFINED)) {
                if (bothTiesUndefined(x, z)) {
                    tieBystanders(x, z, xStatus, zStatus);
                }
                if (bothTiesUndefined(z, y)) {
                    tieBystanders(z, y, zStatus, yStatus);
                }
            }
            /* equation (5),(7) pp. 1379-80 of AJS */
            if (getTies().getTieState(y, x) == ISocialStructure.DOMINATES) {
                if (bothTiesUndefined(z, x)) {
                    tieBystanders(z, x, zStatus, xStatus);
                }
                if (bothTiesUndefined(x, z)) {
                    tieBystanders(y, z, yStatus, zStatus);
                }
            }
        }
    }

    private void handleInteractants(final int x, final int y, final int sc) {

        /* equation (3) p. 1378 of AJS */
        if (bothTiesUndefined(x, y)) {
            tieInteractants(x, y, getStatusData()[x][sc], getStatusData()[y][sc]);
        }
    }

    private void resetData() {

        steps2consensus = 0;
        consensus = false;
        for (int x = 0; x < getBinitial().length; x++) {
            getBupdate()[x] = getBinitial()[x];
        }
        for (int x = 0; x < getGroupSize(); x++) {
            nPathPairs[x][PATH_COUNT_NEG] = 0;
            nPathPairs[x][PATH_COUNT_POS] = 0;
            for (int y = 0; y < getGroupSize(); y++) {
                getParticipationCount()[x][y] = 0;
                getPXaddressY()[x][y] = 0.0;
            }
        }
    }

    @Override
    public void run() {

        resetData();
        long seed = bid * bid * System.currentTimeMillis();
        // System.out.println("       som: seed => " + seed);
        random = new Random(seed);

        for (int i = 0; i < msgLimit; i++) {
            updateNetwork(getNextPair());
            updatePXaddressY();
            getTies().testOrderComplete();
            if (!consensus) {
                testConsensus(i);
            } else {
                break;
            }
        }
        if (logging) {
            String globalState = getTies().stringify();
            logger.info(globalState);
        }
        out.write(decideByVoting());
    }

    public void setBupdate(final double[] val) {
        Bupdate = val;
    }

    public void setDecisionThreshold(final double val) {
        decisionThreshold = val;
    }

    // use once per (control parameters, beliefs, statusMatrix) triple
    private boolean setInitialBeliefs(final double[] val) {

        boolean identical = true;
        for (int x = 0; x < getBinitial().length; x++) {
            getBinitial()[x] = val[x];
            if (val[0] != val[x]) {
                identical = false;
            }
        }
        return identical;
    }

    public void setMsgLimit(final int val) {
        msgLimit = val;
    }

    public void setgroupSize(final int val) {
        setGroupSize(val);
    }

    private void setParameters(final double[] val) {
        // the nominal value for each parameter is divided evenly among
        // dimensions: a kind of Bonferonni adjustment
        eta = val[0] / nDimensions;
        pi = val[1] / nDimensions;
        theta = val[2] / nDimensions;
    }

    public void setStatusData(final StatusValue.Values[][] val) {

        statusData = val;
        nDimensions = val[0].length;
        setGroupSize(val.length);
    }

    private void setTies(final int a, final int b) {

        getTies().setTieState(a, b, ISocialStructure.DOMINATES);
        getTies().setTieState(b, a, ISocialStructure.DEFERS);
        getTies().setRelationCount((getTies().getRelationCount() + 2));
        countPathsToOutcomes(a, b);
    }

    public boolean setup(
            final double[] block,
            final double[] inputBelief) {

        setParameters(block);
        boolean identical = setInitialBeliefs(inputBelief);
        return identical;
    }

    private void testConsensus(final int step) {

        consensus = true;
        double epsilon = 0.0005;

        for (int x = 0; x < getGroupSize(); x++) {
            for (int y = 0; y < getGroupSize(); y++) {
                if ((y != x) &&
                        (epsilon < Math.abs(getBupdate()[x] - getBupdate()[y]))) {
                    consensus = false;
                    break;
                }
            }
        }
        if (consensus) {
            consensusBelief = ((int) (getBupdate()[0] * 1000)) / 1000.0;
            steps2consensus = step;
        }
    }

    private void testNset(final int a, final int b, final double tcut) {
        if ((random.nextDouble() < tcut) && (!ties.isOrderComplete())) {
            setTies(a, b);
        }
    }

    private void tieBystanders(
            final int a,
            final int b,
            final StatusValue.Values aStat,
            final StatusValue.Values bStat) {


        boolean c1 = (getTies().isOrderComplete());
        boolean c2 = (getTies().getTieState(a, b) != ISocialStructure.UNDEFINED);
        boolean c3 = (getTies().getTieState(a, b) != ISocialStructure.UNDEFINED);

        if (c1 || c2 || c3) {
            return;
        }

        double tcut = 0.0;
        if (aStat == bStat) {
            tcut = theta;
        } else if (aStat.ordinal() < bStat.ordinal()) {
            tcut = (1 - eta) * theta;
        } else if (aStat.ordinal() > bStat.ordinal()) {
            tcut = eta + (1 - eta) * theta;
        }
        testNset(a, b, tcut);
    }

    private void tieInteractants(
            final int a,
            final int b,
            final StatusValue.Values aStat,
            final StatusValue.Values bStat) {

        double tcut = 0.0;

        if (aStat == bStat) {
            tcut = pi;
        } else if (aStat.ordinal() < bStat.ordinal()) {
            tcut = pi * (1 - eta);
        } else if (aStat.ordinal() > bStat.ordinal()) {
            tcut = eta + pi * (1 - eta);
        }
        testNset(a, b, tcut);
    }

    // here is where the nominal size of eta must be dealt with
    private void updateNetwork(final int[] pair) {

        int x = pair[0];
        int y = pair[1];

        getParticipationCount()[x][y]++;
        // allow ties to form based on each status characteristic
        for (int sc = 0; sc < nDimensions; sc++) {
            if (!ties.orderComplete) {
                handleInteractants(x, y, sc);
            }
            if (!ties.orderComplete) {
                handleBystanders(x, y, sc);
            }
        }
        if (!consensus) {
            double psFocal = computePS(y, x);
            getBupdate()[x] = getBupdate()[y] * psFocal + (1 - psFocal) * getBupdate()[x];
        }
    }

    private void updatePXaddressY() {

        double sumAes = 0.0, delta = 1.05, mean = 0.0, denom = 0.0;
        double[] aes = new double[getGroupSize()];
        double[] gamma = new double[getGroupSize()];
        ArrayList<Double> tempData = new ArrayList<Double>();
        int x, y;

        // e_factor is derived from Fisek, Berger, and Norman
        // AJS Vol. 97 No 1 1991, p.128
        double e_factor = 0.81737;

        for (x = 0; x < getGroupSize(); x++) {
            aes[x] += -Math.pow(e_factor, nPathPairs[x][PATH_COUNT_POS]);
            aes[x] += Math.pow(e_factor, nPathPairs[x][PATH_COUNT_NEG]);
        }
        mean = sumAes / getGroupSize();
        // calculate MEAN CENTERED aggregate expectations */
        for (x = 0; x < getGroupSize(); x++) {
            gamma[x] = aes[x] - mean;
        }

        double temp;
        /* calculate Prob{ xAy } based on p. 1382 eq. 8 of Skvoretz and Fararo */
        for (x = 0; x < getGroupSize(); x++) {
            for (y = 0; y < getGroupSize(); y++) {
                if (y != x) {
                    temp = Math.exp(gamma[x] + delta * gamma[y]);
                    tempData.add(new Double(temp));
                    getPXaddressY()[x][y] = temp;
                }
            }
        }
        // add temp values in ascending order to reduce numerical problems
        Collections.sort(tempData);
        for (x = 0; x < tempData.size(); x++) {
            denom += tempData.get(x).doubleValue();
        }
        // normalize pXaddressY
        for (x = 0; x < getGroupSize(); x++) {
            for (y = 0; y < getGroupSize(); y++) {
                getPXaddressY()[x][y] /= denom;
            }
        }
    }

    public double[] getBinitial() {
        return Binitial;
    }

    public int[][] getNPathPairs() {
        return nPathPairs;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(final int gSize) {
        groupSize = gSize;
    }

    public int[][] getParticipationCount() {
        return participationCount;
    }

    public void setParticipationCount(final int[][] val) {
        participationCount = val;
    }

    public double[] getBupdate() {
        return Bupdate;
    }

    public Ties getTies() {
        return ties;
    }

    public void setTies(final Ties val) {
        ties = val;
    }

    public double[][] getPXaddressY() {
        return pXaddressY;
    }

    public void setPXaddressY(final double[][] val) {
        pXaddressY = val;
    }

    public StatusValue.Values[][] getStatusData() {
        return statusData;
    }

    /**
     * @return the bid
     */
    public int getBid() {
        return bid;
    }

    /**
     * @param val Assigned to bid
     */
    public void setBid(int val) {
        bid = val;
    }

    /**
     * @param chan Assigned to out
     */
    public void setChan(One2OneChannelInt chan) {
        out = chan.out();
    }

    /**
     * @return the decisionRule
     */
    public int getDecisionRule() {
        return decisionRule;
    }

    /**
     * @param decisionRule the decisionRule to set
     */
    public void setDecisionRule(int decisionRule) {
        this.decisionRule = decisionRule;
    }

    /**
     * @return the consensusBelief
     */
    public double getConsensusBelief() {
        return consensusBelief;
    }
}
