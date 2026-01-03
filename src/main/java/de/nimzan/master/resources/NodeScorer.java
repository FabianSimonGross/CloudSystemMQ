package de.nimzan.master.resources;

import de.nimzan.master.rest.persistence.entity.NodeEntity;

import java.util.Comparator;
import java.util.List;

public class NodeScorer {

    public NodeScorer() {}

    public float computeScore(NodeEntity node) {
        double memFreeRatio = safeDiv(node.getFreeMemory(), node.getMaxMemory());
        double cpu = clamp01(node.getCpuLoad());
        double procCpu = clamp01(node.getProcessCpuLoad());

        double cap = 20.0;
        double runningPenalty = clamp01(node.getRunningServers() / cap);
        double runningScore = 1.0 - runningPenalty;

        double score =
                0.50 * memFreeRatio +
                        0.35 * (1.0 - cpu) +
                        0.10 * (1.0 - procCpu) +
                        0.05 * runningScore;

        return (float) score;
    }

    public List<NodeEntity> rescoreNodes(List<NodeEntity> nodes) {
        if(nodes.isEmpty()) return nodes;

        for (NodeEntity node : nodes) {
            float newScore = this.computeScore(node);
            node.setScore(newScore);
        }

        return nodes;
    }

    public NodeEntity chooseNode(List<NodeEntity> nodes) {
        if (nodes.isEmpty()) return null;

        return nodes.stream().max(Comparator.comparing(NodeEntity::getScore)).orElse(null);
    }

    private static double safeDiv(long a, long b) {
        if (b <= 0) return 0.0;
        return clamp01((double) a / (double) b);
    }


    private static double clamp01(double v) {
        if(v < 0) return 0.0;
        if(v > 1) return 1.0;
        return v;
    }
}
