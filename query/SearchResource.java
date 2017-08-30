package com.ctc.myct.search.query;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.ctc.myct.bo.model.Publication;
import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.model.MultiSearchResult;
import com.ctc.myct.search.model.SearchResult;
import com.ctc.myct.search.util.MyCTLocalServiceHelperUtil;
import com.ctc.myct.search.util.MyCTSearchHelperUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.IndexSearcherHelperUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.hits.HitsProcessorRegistryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Vishnu
 *
 */
@Path("/query")
@SuppressWarnings("rawtypes")
public class SearchResource {
	private static final Log _log = LogFactoryUtil.getLog(SearchResource.class);

	/**
	 * Process the hits from the web content search and returns the JSON
	 * representation of the result. Used to process RSRT, Vimeo and CTU search
	 * hits.
	 *
	 * @param hits
	 * @param searchContext
	 * @param keywords
	 * @param doc
	 * @param type
	 * @return JSON String of JournalArticleId and Suggestions
	 * @throws SearchException
	 * @throws JsonProcessingException
	 */
	private MultiSearchResult createSearchResult(Hits hits, SearchContext searchContext, String keywords, String type)
			throws SearchException, JsonProcessingException {
		String spellCheck = hits.getCollatedSpellCheckResult();
		// the object to be returned as JSON
		MultiSearchResult results = new MultiSearchResult();

		if (Validator.isNull(spellCheck)) {

			spellCheck = IndexSearcherHelperUtil.spellCheckKeywords(searchContext);
		}

		if (!spellCheck.equalsIgnoreCase(keywords)) {
			results.setSuggestion(spellCheck);
		}

		List<Document> doc = hits.toList();

		if ((doc == null) || (doc.size() == 0)) {
			_log.info("No Result Found for " + type);

			return results;
		}

		List<SearchResult> JounralList = new ArrayList<>();
		int index = 0;

		for (Document d : doc) {
			try {
				SearchResult temp = new SearchResult();

				temp.setId(new Long(d.get("articleId")));
				temp.setScore(hits.getScores()[index]);
				++index;
				temp.setType(type);
				temp.setVerison(new Float(d.get("version")));
				JounralList.add(temp);
			} catch (Exception NumberFormatException) {
			}
		}

		results.setResult(JounralList);
		results.setSize(hits.getLength());

		return results;
	}

	/**
	 * @return A json string with source names and suggestion terms.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@GET
	@Path("/myct")
	@Produces("application/json")
	public String doSearch(@QueryParam("keyword") String keywords, @QueryParam("source") String content,
			@Context HttpServletRequest httpRequest) {
		try {
			if (Validator.isNotNull(content)) {
				if (Validator.isNull(keywords)) {
					_log.warn("Invalid keyword : " + keywords);

					return null;
				}

				content = content.trim().toLowerCase();
				keywords = keywords.trim().toLowerCase();

				if (content.contentEquals("all")) {
					return searchAllContents(keywords, httpRequest);
				} else {
					String[] source = content.split(",");

					if (source != null) {
						return searchMultipleContents(keywords, httpRequest, source);
					} else {
						return null;
					}
				}
			} else {
				_log.error("Invalid url parameter source=" + content);

				return null;
			}
		} catch (Exception e) {
			_log.error(e);

			return null;
		}
	}

	private String searchAllContents(String keywords, HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException {
		_log.info("*** Started Searching for All Contents ***");

		return this.searchMultipleContents(keywords, request, "rsrt", "vimeo", "ctu", "publication", "techTraining",
				"bestBets");
	}

	private MultiSearchResult searchCTU(String keywords, HttpServletRequest httpRequest) {
		try {
			_log.info("*** Started Searching for CTU Contents ***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(httpRequest);

			serviceContext.setAttribute("content", "ctu");

			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Hits hits = this.searchWebContent(keywords, httpRequest, searchContext);

			return this.createSearchResult(hits, searchContext, keywords, "ctu");
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage());
			e.printStackTrace();

			return null;
		}
	}

	private String searchMultipleContents(String keywords, HttpServletRequest request, String... contents)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		MultiSearchResult searchResult = new MultiSearchResult();

		for (String content : contents) {
			if (content.equalsIgnoreCase("ctu")) {
				MultiSearchResult multiResult = searchCTU(keywords, request);

				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}

			if (content.equalsIgnoreCase("rsrt")) {
				MultiSearchResult multiResult = searchRsrt(keywords, request);
				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}

			if (content.equalsIgnoreCase("vimeo")) {
				MultiSearchResult multiResult = searchVimeo(keywords, request);

				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}

			if (content.equalsIgnoreCase("techTraining")) {
				MultiSearchResult multiResult = searchTechTraining(keywords, request);

				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}
			if (content.equalsIgnoreCase("bestBets")) {
				MultiSearchResult multiResult = searchBestBets(keywords, request);

				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}

			if (content.equalsIgnoreCase("publication")) {
				MultiSearchResult multiResult = searchPublication(keywords, request);

				searchResult.setResult(multiResult.getResult());
				searchResult.setSize(multiResult.getSize());
				searchResult.setSuggestion(multiResult.getSuggestion());
			}
		}

		MyCTSearchHelperUtil.doSearchSort(searchResult);

		try {
			int start = Integer.parseInt((String) request.getParameter("start"));
			int end = Integer.parseInt((String) request.getParameter("end"));

			MyCTSearchHelperUtil.doPagination(searchResult, start, end);
		} catch (Exception e) {
			_log.warn("Incorrect values for Pagination URL params {start,end}; skipping pagination " + e.toString());
		}

		try {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(request);
			MyCTLocalServiceHelperUtil.fetchContent(searchResult, serviceContext);

		} catch (Exception e) {
			e.printStackTrace();
			// log error
		}
		return mapper.writeValueAsString(searchResult);
	}

	private MultiSearchResult searchBestBets(String keywords, HttpServletRequest httpRequest) {
		try {
			_log.info("***Started Searching for Best Bets Contents***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(httpRequest);

			serviceContext.setAttribute("content", "bestBets");

			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Hits hits = this.searchWebContent(keywords, httpRequest, searchContext);

			return this.createSearchResult(hits, searchContext, keywords, "bestBets");
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e.getLocalizedMessage());

			return null;
		}
	}

	public MultiSearchResult searchPublication(String keywords, HttpServletRequest request) {
		try {
			_log.info("***Started Searching for Publication Contents***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(request);
			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Indexer i = IndexerRegistryUtil.getIndexer(Publication.class.getName());
			Hits hits = i.search(searchContext);
			HitsProcessorRegistryUtil.process(searchContext, hits);
			/**
			 * Following parent-child relations has been temporarily removed to
			 * avoid conflicting with Pagination.
			 *
			 * documents.addAll(getChildJournal(searchContext, keywords));
			 * documents.addAll(getChildFileDocuments(searchContext, keywords));
			 */

			String spellCheck = hits.getCollatedSpellCheckResult();

			// the object to be returned as JSON
			MultiSearchResult results = new MultiSearchResult();

			// publication id's from the hits
			List<SearchResult> publicationResult = new ArrayList<>();
			float[] scores = hits.getScores();
			int index = 0;

			for (Document d : hits.toList()) {
				try {
					SearchResult temp = new SearchResult();

					temp.setId((new Long(d.get("publicationId"))));
					temp.setType("publication");
					temp.setScore(scores[index]);
					++index;
					publicationResult.add(temp);
				} catch (Exception NumberFormatException) {
				}
			}

			results.setResult(publicationResult);
			results.setSize(hits.getLength());

			/**
			 * Adding results from Child JournalArticle Documents
			 */
			MultiSearchResult childSearchResults = processJournalArticleChildHits(searchContext);
			results.setResult(childSearchResults.getResult());
			results.setSize(childSearchResults.getSize());

			/**
			 * Adding results from Child File Documents
			 */
			childSearchResults = processFileEntryChildHits(searchContext);
			results.setResult(childSearchResults.getResult());
			results.setSize(childSearchResults.getSize());

			if (Validator.isNotNull(spellCheck)) {
				results.setSuggestion(spellCheck);
			} else {
				spellCheck = IndexSearcherHelperUtil.spellCheckKeywords(searchContext);

				if (!spellCheck.equalsIgnoreCase(keywords)) {
					results.setSuggestion(spellCheck);
				}
			}

			return results;
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e);

			return new MultiSearchResult();
		}
	}

	private MultiSearchResult processFileEntryChildHits(SearchContext searchContext) throws SearchException {
		Indexer i = IndexerRegistryUtil.getIndexer(DLFileEntry.class.getName());

		Hits childHits = i.search(searchContext);
		List<SearchResult> fileList = new ArrayList<>();
		int index = 0;
/*
 * The part needs a thorugt vheibg 
 */
		_log.info("Searching in File Entries of Publication");
		for (Document d : childHits.toList()) {
			try {
				SearchResult temp = new SearchResult();

				temp.setId(new Long(d.get("publicationId")));
				temp.setScore(childHits.getScores()[index]);
				++index;
				temp.setType("publication");
				temp.setVerison(new Float(d.get("version")));
				fileList.add(temp);
			} catch (Exception NumberFormatException) {
			}
		}
		_log.info("Number of hits from File Entries :" + fileList.size());

		MultiSearchResult results = new MultiSearchResult();
		results.setResult(fileList);
		results.setSize(fileList.size());

		return results;
	}

	private MultiSearchResult processJournalArticleChildHits(SearchContext searchContext) throws SearchException {

		Indexer i = IndexerRegistryUtil.getIndexer(JournalArticle.class.getName());

		Hits childHits = i.search(searchContext);
		List<SearchResult> JounralList = new ArrayList<>();
		int index = 0;

		_log.info("Searching in Journal Articles of Publication");
		for (Document d : childHits.toList()) {
			try {
				SearchResult temp = new SearchResult();

				temp.setId(new Long(d.get("publicationId")));
				temp.setScore(childHits.getScores()[index]);
				++index;
				temp.setType("publication");
				temp.setVerison(new Float(d.get("version")));
				JounralList.add(temp);
			} catch (Exception NumberFormatException) {
			}
		}
		_log.info("Number of hits from Journals :" + JounralList.size());

		MultiSearchResult results = new MultiSearchResult();
		results.setResult(JounralList);
		results.setSize(JounralList.size());

		return results;
	}

	public MultiSearchResult searchRsrt(String keywords, HttpServletRequest request) {
		try {
			_log.info("***Started Searching for RSRT Contents***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(request);

			serviceContext.setAttribute("content", "rsrt");

			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Hits hits = this.searchWebContent(keywords, request, searchContext);

			return this.createSearchResult(hits, searchContext, keywords, "rsrt");
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e.getLocalizedMessage());

			return null;
		}
	}

	/**
	 * 
	 * @param keywords
	 * @param request
	 * @return
	 */
	private MultiSearchResult searchTechTraining(String keywords, HttpServletRequest request) {
		try {
			_log.info("***Started Searching for Tech Training Contents***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(request);

			serviceContext.setAttribute("content", "techTraining");

			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Hits hits = this.searchWebContent(keywords, request, searchContext);

			return this.createSearchResult(hits, searchContext, keywords, "techTraining");
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e.getLocalizedMessage());

			return null;
		}
	}

	private MultiSearchResult searchVimeo(String keywords, HttpServletRequest httpRequest) {
		try {
			_log.info("***Started Searching for Vimeo Contents***");

			ServiceContext serviceContext = ServiceContextFactory.getInstance(httpRequest);

			serviceContext.setAttribute("content", "vimeo");

			SearchContext searchContext = this.getSearchContext(serviceContext, keywords);
			Hits hits = this.searchWebContent(keywords, httpRequest, searchContext);

			return this.createSearchResult(hits, searchContext, keywords, "vimeo");
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e.getLocalizedMessage());

			return null;
		}
	}

	/**
	 * Returns the Search hits from web contents
	 */
	private Hits searchWebContent(String keywords, HttpServletRequest httpRequest, SearchContext searchContext)
			throws PortalException {

		Indexer i = IndexerRegistryUtil.getIndexer(JournalArticle.class.getName());

		try {
			Hits hits = i.search(searchContext);

			return hits;
		} catch (Exception e) {
			e.printStackTrace();

			return new HitsImpl();
		}
	}

	/**
	 *
	 * @param serviceContext
	 * @param keywords
	 * @return SearchContext
	 */
	private SearchContext getSearchContext(ServiceContext serviceContext, String keywords) {
		SearchContext searchContext = SearchContextFactory.getInstance(serviceContext.getAssetCategoryIds(),
				serviceContext.getAssetTagNames(), serviceContext.getAttributes(), serviceContext.getCompanyId(),
				keywords, null, serviceContext.getLocale(), ReadConfig.getSiteId(), serviceContext.getTimeZone(),
				serviceContext.getUserId());

		searchContext.setAttributes(serviceContext.getAttributes());
		searchContext.setStart(0);
		searchContext.setEnd(500);
		searchContext.setAttribute("pagination", "more");

		return searchContext;
	}
}