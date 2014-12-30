package com.graphaware.reco.neo4j.engine;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.filter.Filter;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import org.neo4j.graphdb.Node;

import java.util.*;

/**
 * {@link SingleScoreRecommendationEngine} that randomly recommends {@link org.neo4j.graphdb.Node}s which comply with
 * the provided {@link com.graphaware.common.policy.NodeInclusionPolicy}.
 */
public abstract class RandomEngine extends SingleScoreRecommendationEngine<Node, Node> {

    private final NodeSelector selector;

    public RandomEngine() {
        this.selector = new RandomNodeSelector(getPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipationPolicy<Node, Node> participationPolicy(Context context) {
        //noinspection unchecked
        return ParticipationPolicy.IF_MORE_RESULTS_NEEDED;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A maximum of {@link com.graphaware.reco.generic.context.Context#limit()} number of nodes is returned, each with
     * a score determined by {@link #score(org.neo4j.graphdb.Node)}. The total number of attempts made to find a suitable
     * node is determined by {@link #numberOfAttempts(com.graphaware.reco.generic.context.Context)}.
     */
    @Override
    protected final Map<Node, Integer> doRecommend(Node input, Context<Node, Node> context) {
        Map<Node, Integer> result = new HashMap<>();
        int attempts = 0;

        while (attempts++ < numberOfAttempts(context) && result.size() < context.limit()) {
            Node node = selector.selectNode(input.getGraphDatabase());
            result.put(node, score(node));
        }

        return result;
    }

    /**
     * Score a randomly selected node.
     *
     * @param node to score.
     * @return score, 1 by default.
     */
    protected int score(Node node) {
        return 1;
    }

    /**
     * Determine the maximum total number of attempts to make when selecting random nodes to recommend.
     *
     * @param context of the current computation.
     * @return maximum number of attempts.
     */
    protected int numberOfAttempts(Context<Node, Node> context) {
        return context.limit() * 10;
    }

    /**
     * Get the node inclusion policy of the nodes that can be used as recommendations.
     *
     * @return policy.
     */
    protected abstract NodeInclusionPolicy getPolicy();
}