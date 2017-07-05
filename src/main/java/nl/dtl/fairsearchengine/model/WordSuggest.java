package nl.dtl.fairsearchengine.model;

public class WordSuggest {
	String text;
	int score;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
}
