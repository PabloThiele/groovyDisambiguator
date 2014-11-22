package utils.model;

/**
 * Created by pablo_thiele on 11/13/2014.
 */
public class WordTag {
    private String word;
    private String tag;


    public WordTag(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
