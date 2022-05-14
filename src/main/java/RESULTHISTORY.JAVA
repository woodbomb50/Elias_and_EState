import java.util.LinkedHashSet;
import java.util.Set;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author Roy Wilson
 */
public class ResultHistory {
    
    private Matrix x0;
    private DenseVector y0;
    private double[] pval;
    private Set<Integer> stricken = new LinkedHashSet<Integer>();
     
    /**
     * This constructor initializes the result history using the full model
     * matrix
     * @param x Reduced model matrix
     * @param y Reduced result vector
     */
    public ResultHistory(Matrix x, DenseVector y) {

        // System.out.println("RH: p2c.size => " + p2c.size());
        x0 = x;
        // System.out.println("RH: x.numColumns => " + x.numColumns());
        y0 = y;

        // run R glm-like program using x0 and y0 and extract pvalues
        // pval is indexed the column of the panelist
        // Random random = new Random(System.currentTimeMillis());
        /**
        for (int i = 0; i < x.numColumns(); i++) {
            pval[i] = random.nextDouble();
        }
        **/
    }

    /**
     * @return the x0
     */
    public Matrix getX0() {
        return x0;
    }

    /**
     * @return the y0
     */
    public DenseVector getY0() {
        return y0;
    }

    /**
     * @return the pval
     */
    /*
    public double[] getPval() {
        return pval;
    }
    */

}
