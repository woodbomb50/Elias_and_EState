public interface ITie {

    void setState(final int state);

    void display(final String msg);
    
    int UNDEFINED_STATUS = 0;

    int X_COMPONENT = 2;

    int X_DEFERS_TO_Y = -1;

    int X_DOMINATES_Y = 1;

    int Y_COMPONENT = -2;
}

