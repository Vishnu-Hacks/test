package com.ctc.myct.search.model;

import com.ctc.myct.search.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class SearchResult implements Comparable<SearchResult> {
	@JsonIgnore
	Long id;

	// @JsonIgnore
	float score;

	String type;

	// @JsonIgnore
	float verison;

	BaseDTO content;

	public BaseDTO getContent() {
		return content;
	}

	public void setContent(BaseDTO content) {
		this.content = content;
	}

	public float getVerison() {
		return verison;
	}

	public void setVerison(float verison) {
		this.verison = verison;
	}

	@Override
	public int compareTo(SearchResult s1) {
		return Float.compare(s1.getScore(), this.score);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
