package edu.kpi.iasa.ai;

import edu.kpi.iasa.ai.gui.DrawZone;
import edu.kpi.iasa.ai.gui.drawable.APoint;
import edu.kpi.iasa.ai.model.MapNode;

import java.util.*;

public class Converter {

    public static Map<APoint, List<APoint>> graph(Map<MapNode, Set<MapNode>> graph, DrawZone parent) {
        if(graph == null) return null;
        Map<APoint, List<APoint>> res = new HashMap<>();
        for(MapNode key : graph.keySet()) {
            List<APoint> list = new ArrayList<>();
            if(graph.get(key) != null) {
                for (MapNode mn : graph.get(key)) {
                    list.add(new APoint(mn.getLocation(), parent));
                }
            }
            res.put(new APoint(key.getLocation(),parent),list);
        }
        return res;
    }

    public static Map<APoint, APoint> tree(Map<MapNode, MapNode> tree, DrawZone parent) {
        if(tree == null) return null;
        Map<APoint, APoint> res = new HashMap<>();
        for(MapNode key : tree.keySet()) {
            MapNode value = tree.get(key);
            if(value != null) {
                res.put(new APoint(key.getLocation(), parent), new APoint(value.getLocation(), parent));
            }
        }
        return res;
    }


    public static List<APoint> path(List<MapNode> path, DrawZone parent) {
        if(path == null) return null;
        List<APoint> res = new ArrayList<>();
        if(path != null) {
            for (MapNode mn : path) {
                res.add(new APoint(mn.getLocation(), parent));
            }
        }
        return res;
    }
}
