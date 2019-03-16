package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.configuration.Configured;
import edu.kpi.iasa.ai.configuration.ConfiguredObstacle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObstacleContainer implements Configured<List<ConfiguredObstacle>> {

    private List<ConfiguredObstacle> obstacles = new ArrayList<>();

    private boolean modifiable = true;

    public List<ConfiguredObstacle> nonBlocking = new ArrayList<>();

    public List<ConfiguredObstacle> getObstacles() {
        return obstacles;
    }

    public void addObstacle(ConfiguredObstacle obstacle) {
        if(obstacle == null) {
            return;
        }
        if(this.obstacles == null) {
            this.obstacles = new ArrayList<>(1);
        }
        this.obstacles.add(obstacle);
    }

    public void addObstacles(Collection<ConfiguredObstacle> obstacles) {
        if(obstacles == null) {
            return;
        }
        if(this.obstacles == null) {
            this.obstacles = new ArrayList<>(obstacles.size());
        }
        for(ConfiguredObstacle obst: obstacles){
            addObstacle(obst);
        }
    }

    public void addNonBlocking(ConfiguredObstacle nonBlocking) {
        if(nonBlocking == null) {
            return;
        }
        if(this.nonBlocking == null) {
            this.nonBlocking = new ArrayList<>(1);
        }
        this.nonBlocking.add(nonBlocking);
    }

    public void forceRedraw(){
        for(ConfiguredObstacle obst : obstacles) {
            obst.getValue().invalidate();
        }
        for(ConfiguredObstacle obst : nonBlocking) {
            obst.getValue().invalidate();
        }
    }

    public List<ConfiguredObstacle> getNonBlocking() {
        return nonBlocking;
    }

    public void setNonBlocking(List<ConfiguredObstacle> nonBlocking) {
        this.nonBlocking = nonBlocking;
    }

    public void setObstacles(List<ConfiguredObstacle> obstacles) {
        this.obstacles = obstacles;
    }

    @Override
    public void setValue(List<ConfiguredObstacle> value) {
        setObstacles(value);
    }

    @Override
    public List<ConfiguredObstacle> getValue() {
        return getObstacles();
    }

    @Override
    public void setModifiable(boolean state) {
        modifiable = state;
    }

    @Override
    public boolean isModifiable() {
        return modifiable;
    }
}
