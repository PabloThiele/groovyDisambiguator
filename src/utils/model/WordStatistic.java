package utils.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pablo_Thiele on 11/18/2014.
 */
public class WordStatistic {

    private String word;
    private Integer totalHits;
    private List<TagStatistic> tagStatistic;

    public WordStatistic(String word, Integer totalHits, List<TagStatistic> tagStatistic) {
        this.word = word;
        this.totalHits = totalHits;
        this.tagStatistic = tagStatistic;
    }

    public WordStatistic() {
        this.totalHits = 0;
        this.tagStatistic = new ArrayList<TagStatistic>();
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(Integer totalHits) {
        this.totalHits = totalHits;
    }

    public List<TagStatistic> getTagStatistic() {
        return tagStatistic;
    }

    public void setTagStatistic(List<TagStatistic> tagStatistic) {
        this.tagStatistic = tagStatistic;
    }
}
