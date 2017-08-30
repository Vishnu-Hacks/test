package com.ctc.myct.search.model;

public class TypeAhead implements Comparable<TypeAhead> {
	String title;
	float score;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	@Override
	public int compareTo(TypeAhead o) {
		return Float.compare(o.getScore(), this.score);
	}

}
