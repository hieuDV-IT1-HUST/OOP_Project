package visualization;

public class PageRankEntry {
    private final String node;
    private final String score;

    public PageRankEntry(String node, String score) {
        this.node = node;
        this.score = score;
    }

    public String getNode() {
        return node;
    }

    public String getScore() {
        return score;
    }
}
