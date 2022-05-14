public class Tie implements ITie {

    private int a;
    private int b;
    private int state;

    public Tie(final int x, final int y) {

        a = x;
        b = y;
        state = ISocialStructure.UNDEFINED;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public Tie(final int x, final int y, final int val) {

        a = x;
        b = y;
        state = val;
    }

    @Override
    public void display(final String msg) {

        String relation;

        if (getA() == getB()) {
            return;
        }

        switch (getState()) {
            case ISocialStructure.UNDEFINED:
                relation = "UNDEFINED";
                break;
            case ISocialStructure.DOMINATES:
                relation = "DOMINATES";
                break;
            case ISocialStructure.DEFERS:
                relation = "DEFERS";
                break;
            default:
                relation = "UNKNOWN STATE!";
                break;
        }
        System.out.println(msg + " Person: " + getA() + " " + relation + " Person: " + getB());
    }

    /**
     *
     * @param tie
     * @return
     */
    public boolean equals(final Tie tie) {
        return (getA() == tie.getA()) && (getB() == tie.getB());
    }

    public boolean isDefined() {
        return (getState() != ISocialStructure.UNDEFINED);
    }

    /**
     *
     * @param val
     */
    @Override
    public void setState(final int val) {
        state = val;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getState() {
        return state;
    }
}

