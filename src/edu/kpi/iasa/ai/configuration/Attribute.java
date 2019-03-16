package edu.kpi.iasa.ai.configuration;

public interface Attribute {

    String OBSTACLE_CONTAINER = "ObstacleContainer";

    String OBSTACLE = "Obstacle";



    String MAX_ITERATIONS_COUNT = "Max iterations count";

    String SHAP_SIZE = "Obstacle size";

    String SLEEP_TIME_PER_STEP = "Time to draw single iteration";

    static boolean isConstToSimulation(String attribute) {
        return SHAP_SIZE.equals(attribute)
                || MAX_ITERATIONS_COUNT.equals(attribute);
    }

    static String getObstacleAttribute(long index) {
        return OBSTACLE+index;
    }

}
