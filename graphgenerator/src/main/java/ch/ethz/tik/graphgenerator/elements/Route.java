package ch.ethz.tik.graphgenerator.elements;

import java.io.Serializable;
import java.util.List;

import ch.ethz.tik.graphgenerator.util.Constants;

public class Route implements Serializable {

    static final long serialVersionUID = -8115775569775087169L;

    private SearchNode from;
    private SearchNode to;
    private List<Node> shortestPath;
    private List<Node> hOptPath;
    private int[] shortestPathScores;
    private int[] hOptPathScores;

    public static class Builder {
        private SearchNode from;
        private SearchNode to;

        private List<Node> shortestPath;
        private List<Node> hOptPath;
        private int[] shortestPathScores;
        private int[] hOptPathScores;

        public Builder(SearchNode from, SearchNode to) {
            this.from = from;
            this.to = to;

            this.shortestPathScores = new int[2];
            this.hOptPathScores = new int[2];
        }

        public Builder shortestPath(List<Node> path) {
            this.shortestPath = path;
            return this;
        }

        public Builder hOptPath(List<Node> path) {
            this.hOptPath = path;
            return this;
        }

        public Builder shortestPathScores(int poll, int dist) {
            this.shortestPathScores[Constants.INDEX_POLLUTION] = poll;
            this.shortestPathScores[Constants.INDEX_DISTANCE] = dist;
            return this;
        }

        public Builder hOptPathScores(int poll, int dist) {
            this.hOptPathScores[Constants.INDEX_POLLUTION] = poll;
            this.hOptPathScores[Constants.INDEX_DISTANCE] = dist;
            return this;
        }

        public Route build() {
            return new Route(this);
        }

    }

    private Route(Builder builder) {
        from = builder.from;
        to = builder.to;
        shortestPath = builder.shortestPath;
        hOptPath = builder.hOptPath;
        shortestPathScores = builder.shortestPathScores;
        hOptPathScores = builder.hOptPathScores;
    }

    public void setFrom(SearchNode node) {
        from = node;
    }

    public void setTo(SearchNode node) {
        to = node;
    }

    public void setShortestPath(List<Node> path) {
        shortestPath = path;
    }

    public void setHOptPath(List<Node> path) {
        hOptPath = path;
    }

    public void setShortestPathScores(int poll, int dist) {
        shortestPathScores[Constants.INDEX_POLLUTION] = poll;
        shortestPathScores[Constants.INDEX_DISTANCE] = dist;
    }

    public void setHOptPathScores(int poll, int dist) {
        hOptPathScores[Constants.INDEX_POLLUTION] = poll;
        hOptPathScores[Constants.INDEX_DISTANCE] = dist;
    }

    public void setShortestPathScores(int[] scores) {
        shortestPathScores[Constants.INDEX_POLLUTION] = scores[Constants.INDEX_POLLUTION];
        shortestPathScores[Constants.INDEX_DISTANCE] = scores[Constants.INDEX_DISTANCE];
    }

    public void setHOptPathScores(int[] scores) {
        hOptPathScores[Constants.INDEX_POLLUTION] = scores[Constants.INDEX_POLLUTION];
        hOptPathScores[Constants.INDEX_DISTANCE] = scores[Constants.INDEX_DISTANCE];
    }

    public SearchNode getFrom() {
        return from;
    }

    public SearchNode getTo() {
        return to;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public List<Node> getHOptPath() {
        return hOptPath;
    }

    public int[] getShortestPathScores() {
        return shortestPathScores;
    }

    public int[] getHOptPathScores() {
        return hOptPathScores;
    }

    public void reset() {
        from = null;
        to = null;
        shortestPath = null;
        hOptPath = null;
    }
}
