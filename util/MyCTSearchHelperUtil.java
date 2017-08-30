package com.ctc.myct.search.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.model.MultiSearchResult;
import com.ctc.myct.search.model.SearchResult;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class MyCTSearchHelperUtil {

	private static final Log _log = LogFactoryUtil.getLog(ReadConfig.class);

	public static void doPagination(MultiSearchResult searchResult, int start, int end) {
		_log.info("*** Staring Pagination ***");

		if (start > end) {
			int temp = start;
			start = end;
			end = temp;
			_log.info("'start' param greater than 'end' , swapping pagination values");
		}

		if (end <= searchResult.getSize()) {
			List<SearchResult> paginatedResult = searchResult.getResult().subList(start, end);

			searchResult.updateResult(paginatedResult);
		} else {
			_log.warn("Pagination param end=" + end + " higher than result size : " + searchResult.getSize());

			_log.warn("Re-doing pagination with end=" + searchResult.getSize());
			doPagination(searchResult, start, searchResult.getSize());
		}
	}

	public static void doSearchSort(MultiSearchResult searchResult) {
		/**
		 * removing duplicates
		 */
		_log.debug("Removing duplicates values : size before - " + searchResult.getSize());
		HashMap<String, SearchResult> hashMap = new HashMap<>();

		for (SearchResult i : searchResult.getResult()) {
			if (hashMap.containsKey(i.getId() + i.getType())) {

				searchResult.decrementSize();
			} else

				hashMap.put(i.getId() + i.getType(), i);
		}
		List<SearchResult> uniqueResults = new LinkedList<SearchResult>();
		uniqueResults.addAll(hashMap.values());

		searchResult.updateResult((uniqueResults));
		_log.debug("Size after - " + searchResult.getSize());

		_log.info("*** Staring Relevancy Sorting ***");
		Collections.sort(searchResult.getResult());
	}
}
