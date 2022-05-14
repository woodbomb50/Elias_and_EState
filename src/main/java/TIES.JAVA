import java.util.ArrayList;

/**
 * Each group is represented by a set of nodes, where each node corresponds to a
 * person. All computation is defined in terms of a set of nodes numbered from 0
 * to n-1, where n is the number of nodes.
 * 
 */
public class Ties implements ISocialStructure {

    boolean orderComplete = false;
    int steps2equilibrium = 0;
    int relationCount = 0;
    private ArrayList<Tie> network = new ArrayList<Tie>();
    private String[][] R;
    int nPersons = 0;
    int maxTies = 0;
    int interactionCount = 0;

    /**
     * This class represents all possible ties between nPersons and the value of
     * various state variables that change as the number of ties formed change.
     *
     * @param n
     */
    public Ties(int n) {

        if ((n < 0) || (n > ISocialStructure.MAX_NODE)) {
            System.out.println("Error: Ties: n > ISocialStructure.MAX_NODE => " + ISocialStructure.MAX_NODE);
            System.exit(0);
        }
        nPersons = n;
        R = new String[nPersons][nPersons];
        maxTies = n * (n - 1);
        network.clear();
        for (int x = 0; x < nPersons; x++) {
            // self included to make indexing easier
            for (int y = 0; y < nPersons; y++) {
                add(new Tie(x, y));
            }
        }
    }

    public void add(Tie tie) {
        getNetwork().add(tie);
    }

    public void clear() {
        getNetwork().clear();
    }

    /**
     * @return Returns the relationCount.
     */
    public int getRelationCount() {
        return relationCount;
    }

    /**
     * @return step2equilibrium.
     */
    public int getSteps2equilibrium() {
        return steps2equilibrium;
    }

    /**
     * @param index
     * @return A tie.
     */
    public Tie getTie(int index) {

        int x = index / nPersons;
        int y = index % nPersons;
        return getTie(x, y);
    }

    /**
     * @param x
     * @param y
     * @return tie
     */
    public Tie getTie(int x, int y) {
        return getNetwork().get(indexOf(x, y));
    }

    /**
     * @return The number of ties in the structure.
     */
    public int getTieCount() {
        return getNetwork().size();
    }

    public int getTieState(int x, int y) {
        return getNetwork().get(indexOf(x, y)).getState();
    }

    /**
     * @param x
     * @param y
     * @return The index of tie (x, y) in the tie list.
     */
    public int indexOf(int x, int y) {
        return (x * nPersons + y);
    }

    public boolean isOrderComplete() {
        return orderComplete;
    }

    /**
     * @param count
     *            The value assigned to relationCount.
     */
    public void setRelationCount(int count) {
        relationCount = count;
    }

    /**
     *
     *
     * @param val
     */
    public void setSteps2equilibrium(int val) {
        steps2equilibrium = val;
    }

    /**
     * @param x
     * @param y
     * @param state
     */
    public void setTieState(int x, int y, int state) {
        getTie(x, y).setState(state);
    }

    public void buildR() {

        int x, y;
        short[] tieCount = new short[nPersons];

        for (x = 0; x < nPersons; x++) {
            for (y = 0; y < nPersons; y++) {
                R[x][y] = "u";
            }
        }

        Tie tie;

        for (int i = 0; i < getTieCount(); i++) {
            tie = getTie(i);
            x = tie.getA();
            y = tie.getB();
            switch (tie.getState()) {
                case ITie.X_DOMINATES_Y:
                    R[x][y] = "D";
                    tieCount[x]++;
                    R[y][x] = "d";
                    break;
                case ITie.X_DEFERS_TO_Y:
                    R[x][y] = "d";
                    R[y][x] = "D";
                    tieCount[y]++;
                    break;
                default:
            }
        }
    }

    public void show() {

        int x, y;

        short[] tieCount = new short[nPersons];

        if (0 < steps2equilibrium) {
            System.out.println("Structural equilibrium reached at step " + steps2equilibrium);
        } else {
            System.out.println("Structural equilibrium not yet reached");
        }

        buildR();

        System.out.println();
        for (x = 0; x < nPersons; x++) {
            System.out.print("   " + x);
        }
        System.out.println();
        for (x = 0; x < nPersons; x++) {
            R[x][x] = "--";
            System.out.print(x + " ");
            for (y = 0; y < nPersons; y++) {
                System.out.print(R[x][y] + " ");
            }
            System.out.println(tieCount[x]);
        }
    }

    public String stringify() {

        String result = "";

        buildR();

        for (int x = 0; x < nPersons; x++) {
            for (int y = 0; y < nPersons; y++) {
                result += R[x][y];
            }
        }
        return result;
    }

    public void testOrderComplete() {

        interactionCount++;
        if (!orderComplete && (relationCount == maxTies)) {
            orderComplete = true;
            steps2equilibrium = interactionCount;
        }
    }

    /**
     * @return the network
     */
    public ArrayList<Tie> getNetwork() {
        return network;
    }

    /**
     * @return the R
     */
    public String[][] getR() {
        
        initializeR();

        Tie tie;

        int x, y;

        for (int i = 0; i < getTieCount(); i++) {
            tie = getTie(i);
            x = tie.getA();
            y = tie.getB();
            switch (tie.getState()) {
                case ITie.X_DOMINATES_Y:
                    R[x][y] = "D";
                    R[y][x] = "d";
                    break;
                case ITie.X_DEFERS_TO_Y:
                    R[x][y] = "d";
                    R[y][x] = "D";
                    break;
                default:
            }
        }
        return R;
    }

    /**
     */
    private void initializeR() {

        for (int x = 0; x < nPersons; x++) {
            for (int y = 0; y < nPersons; y++) {
                R[x][y] = "u";
            }
        }
        
    }
}
