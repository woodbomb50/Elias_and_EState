/**
 * @author Roy Wilson
 * 3/29/22
 *
 * ISocialStructure provides the values of parameters that relate to
 * the social structure of a small group.
 */
public interface ISocialStructure {
	// maximum number of nodes in a group such that 
	// sequential nature of interaction is maintained
    int MAX_NODE = 5;  
    int MIN_NODEID = 0; // MIN_NODEID is the node address
    int MAX_NODEID = MAX_NODE - 1;
    // MAX_EDGE = # of edges in a complete uni-directed graph
    // given by MAX_NODE * ( MAX_NODE - 1 ) / 2; 
    int MAX_EDGE = MAX_NODE * ( MAX_NODE - 1 ); 
    // but a -> b different than b-> a, so twice as many edges are possible

    int RANDOM_UNIFORM = 0;

    /* CHECK NEXT LINE */

    int INTERACTION_LIMIT = 10 * MAX_NODE;

    // tie states
    int UNDEFINED = 0;
    int DOMINATES = 1;
    int DEFERS = 2;

    // status information
    // in the simplest ranking, the members are ranked from
    // 1 to MAX_NODE
    double STATUS_MIN = 1.0;
    double STATUS_MAX = MAX_NODE * 1.0;

    // final double STATUS_LO = STATUS_MIN;
    // final double STATUS_HI = STATUS_MAX;
    
    int ALL_ZEROS = 0;
    int UNIFORM_ZERO_OFF_DIAGONAL = 1;
    int ONE_ELEMENT_EQUAL_TO_1 = 2;
    
    int REPORTER_AD = 0;

    int REPORTER_SW = 1;

    int REPORTER_NU = 2;

    int REPORTER_TE = 3;

    int REPORTER_CO = 4;
    
    int STATUS_PATTERN_HHLLL = 5;
    
    int STATUS_PATTERN_HHHLL = 6;
}