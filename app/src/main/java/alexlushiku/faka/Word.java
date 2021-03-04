package alexlushiku.faka;

import android.content.SharedPreferences;

public class Word {

    private String word;
    private String[] meanings;
    private String[] examples;
    private boolean isFavorite;

    public Word(String word, String[] meanings, String[] examples) {
        this.word = word;
        this.meanings = meanings;
        this.examples = examples;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning(int position) {
        return meanings[position];
    }

    public String getExample(int position) {
        return examples[position];
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setMeanings(String[] meaning) {
        this.meanings = meaning;
    }

    public void setExamples(String[] example) {
        this.examples = example;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getSize() {
        return meanings.length;
    }
}
