package com.ctc.myct.search.model;

import java.util.List;

public class TypeAheadSearchResult {

	String word;
	List<TypeAhead> typeAheads;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public List<TypeAhead> getTypeAheads() {
		return typeAheads;
	}

	public void setTypeAheads(List<TypeAhead> typeAheads) {
		this.typeAheads = typeAheads;
	}

}
