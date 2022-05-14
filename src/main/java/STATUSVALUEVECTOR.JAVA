import java.io.Serializable;
import java.util.Random;

public class StatusValueVector implements Serializable {

    private static final long serialVersionUID = 1L;
    private StatusValue.Values[] data;
    private int dimensions;

    public StatusValueVector(int dimensions) {

        this.dimensions = dimensions;

        this.data = new StatusValue.Values[this.dimensions];

        Random temp = new Random(System.currentTimeMillis());

        for (int j = 0; j < dimensions; j++) {
            if (temp.nextInt(2) == 0) {
                this.data[j] = StatusValue.Values.LO;
            } else {
                this.data[j] = StatusValue.Values.HI;
            }
        }
    }

    /*
    private void generateRandomly(int dimensions) {

        this.dimensions = dimensions;

        this.data = new StatusValue.Values[this.dimensions];

        Random temp = new Random(System.currentTimeMillis());

        for (int j = 0; j < dimensions; j++) {
            if (temp.nextInt(2) == 0) {
                this.data[j] = StatusValue.Values.LO;
            } else {
                this.data[j] = StatusValue.Values.HI;
            }
        }
    }
    */

    public StatusValue.Values[] getData() {
        return this.data;
    }

    public boolean compare(StatusValueVector other) {

        boolean retval = true;

        StatusValue.Values[] test = other.getData();
        for (int j = 0; j < this.dimensions; j++) {
            if (this.data[j].ordinal() != test[j].ordinal()) {
                retval = false;
                break;
            }
        }
        return retval;
    }

    public void show(String msg) {

        System.out.println(msg);

        for (int i = 0; i < data.length; i++) {
            System.out.print(this.data[i] + " ");
        }
        System.out.println();
    }
}
