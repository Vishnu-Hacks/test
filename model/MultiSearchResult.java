package com.ctc.myct.search.model;

import java.util.LinkedList;
import java.util.List;

public class MultiSearchResult {
	List<SearchResult> result;
	String suggestion;
	int size;

	public MultiSearchResult() {
		size = 0;
		result = new LinkedList<>();
	}

	public List<SearchResult> getResult() {
		return result;
	}

	public void setResult(List<SearchResult> result) {
		this.result.addAll(result);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size += size;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {

		this.suggestion = suggestion;
	}

	public int decrementSize() {

		return --size;
	}

	public List<SearchResult> updateResult(List<SearchResult> results) {
		this.result = results;
		return this.result;
	}
}
