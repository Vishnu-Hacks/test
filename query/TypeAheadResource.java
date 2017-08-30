package com.ctc.myct.search.query;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.ctc.myct.bo.model.Publication;
import com.ctc.myct.search.configuration.MyCTSearchConfig;
import com.ctc.myct.search.configuration.ReadConfig;
import com.ctc.myct.search.model.FieldName;
import com.ctc.myct.search.model.TypeAhead;
import com.ctc.myct.search.model.TypeAheadSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexSearcherHelperUtil;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.search.generic.WildcardQueryImpl;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;

@Path("/suggest")
@Component(configurationPid = "com.ctc.myct.search.configuration.MyCTSearchConfig")
public class TypeAheadResource {
	private static final Log _log = LogFactoryUtil.getLog(TypeAheadResource.class);

	@GET
	@Path("/typeahead")
	@Produces("application/json")
	public String doTypeAhead(@QueryParam("word") String keywords, @QueryParam("source") String content,
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
					String[] sources = { "rsrt", "ctu", "publication", "vimeo", "techTraining", "bestBets" };
					return searchMultipleContents(keywords, httpRequest, sources);
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

	private String searchMultipleContents(String keywords, HttpServletRequest request, String... contents)
			throws Exception {
		TypeAheadSearchResult typeAheadResult = new TypeAheadSearchResult();
		typeAheadResult.setWord(keywords);
		keywords = "*" + keywords + "*";

		BooleanFilter booleanFilter = new BooleanFilter();
		ObjectMapper mapper = new ObjectMapper();

		ServiceContext serviceContext = ServiceContextFactory.getInstance(request);

		SearchContext searchContext = SearchContextFactory.getInstance(serviceContext.getAssetCategoryIds(),
				serviceContext.getAssetTagNames(), serviceContext.getAttributes(), serviceContext.getCompanyId(),
				keywords, null, serviceContext.getLocale(), ReadConfig.getSiteId(), serviceContext.getTimeZone(),
				serviceContext.getUserId());

		searchContext.setAttributes(serviceContext.getAttributes());
		searchContext.setStart(0);
		searchContext.setEnd(500);
		searchContext.setAttribute("pagination", "more");
		searchContext.setKeywords(keywords);

		List<TypeAhead> typeAheads = new LinkedList<>();

		BooleanFilter sourceFilter = new BooleanFilter();
		boolean journalSearch = false;
		for (String content : contents) {

			if (content.equalsIgnoreCase("rsrt")) {

				Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.RSRTDDMStructureKey());
				sourceFilter.add(filter, BooleanClauseOccur.SHOULD);
				journalSearch = true;
			}

			if (content.equalsIgnoreCase("vimeo")) {

				Filter filter = sourceFilter.addTerm("ddmStructureKey", _configuration.VimeoDDMStructureKey());
				sourceFilter.add(filter, BooleanClauseOccur.SHOULD);
				journalSearch = true;
			}

			if (content.equalsIgnoreCase("techTraining")) {

				Filter filter = sourceFilter.addTerm("ddmStructureKey", _configuration.TECHTRAININGStructureKey());
				sourceFilter.add(filter, BooleanClauseOccur.SHOULD);
				journalSearch = true;
			}
			if (content.equalsIgnoreCase("bestBets")) {
				Filter filter = sourceFilter.addTerm("ddmStructureKey", _configuration.BestBetsDDMStructureKey());
				sourceFilter.add(filter, BooleanClauseOccur.SHOULD);
				journalSearch = true;

			}
			if (content.equalsIgnoreCase("ctu")) {

				searchCTUTypeAheads(searchContext, typeAheads);

			}

			if (content.equalsIgnoreCase("publication")) {

				searchPublicationTypeAheads(searchContext, typeAheads);
			}
		}

		if (journalSearch) {
			// filtering based on site D
			Filter siteFilter = booleanFilter.addTerm(Field.GROUP_ID, _configuration.GroupId());

			booleanFilter.add(booleanFilter.addTerm(Field.ENTRY_CLASS_NAME, JournalArticle.class.getName()),
					BooleanClauseOccur.MUST);
			booleanFilter.add(booleanFilter.addTerm("ddmStructureKey", _configuration.CTUDDMStructureKey()),
					BooleanClauseOccur.MUST_NOT);
			booleanFilter.add(siteFilter, BooleanClauseOccur.MUST);
			booleanFilter.add(sourceFilter, BooleanClauseOccur.MUST);

			BooleanQuery boolQuery = new BooleanQueryImpl();
			try {
				boolQuery.add(new WildcardQueryImpl("title", keywords), BooleanClauseOccur.SHOULD);
				boolQuery.add(new MatchQuery("title", keywords), BooleanClauseOccur.SHOULD);

			} catch (ParseException e) {
				e.printStackTrace();
				_log.warn(e);
			}

			boolQuery.setPostFilter(booleanFilter);

			Hits hits = null;
			try {
				hits = IndexSearcherHelperUtil.search(searchContext, boolQuery);
			} catch (SearchException e) {
				e.printStackTrace();
				_log.warn(e);
				return mapper.writeValueAsString(typeAheadResult);
			}
			processHits(hits, typeAheads, "title");
		}

		doSorting(typeAheads);
		typeAheads = doPagination(typeAheads);

		typeAheadResult.setTypeAheads(typeAheads);

		return mapper.writeValueAsString(typeAheadResult);

	}

	private void searchPublicationTypeAheads(SearchContext searchContext, List<TypeAhead> typeAheads) throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		User user = UserLocalServiceUtil.getUser(searchContext.getUserId());
		List<Role> user_roles = user.getRoles(); // get user level roles

		// reading all inherited roles of user from the user_groups
		List<UserGroup> user_groups = user.getUserGroups();

		for (UserGroup ug : user_groups) {
			user_roles.addAll(RoleLocalServiceUtil.getGroupRoles(ug.getGroupId()));
		}

		BooleanFilter roleFilter = new BooleanFilter();
		Boolean hasMyCTStoreRole = false;

		for (Role role : user_roles) {
			try {
				if ((boolean) role.getExpandoBridge().getAttribute(FieldName.MyCTStoreRoleExpandoFieldName) == true) {

					Filter filter = roleFilter.addTerm(Field.ROLE_ID, role.getRoleId());

					roleFilter.add(filter, BooleanClauseOccur.SHOULD);
					hasMyCTStoreRole = true;
				}
			} catch (Exception e) {
				_log.error("No Custom Field " + FieldName.MyCTStoreRoleExpandoFieldName + " found in role "
						+ role.getName());
				_log.warn(e.getLocalizedMessage());

				Filter filter = roleFilter.addTerm(Field.ROLE_ID, -1);

				roleFilter.add(filter, BooleanClauseOccur.SHOULD);
			}
		}

		// if user has none of the MyCT roles
		if (!hasMyCTStoreRole) {
			Filter filter = roleFilter.addTerm(Field.ROLE_ID, -1);

			roleFilter.add(filter, BooleanClauseOccur.SHOULD);
			_log.info("User has no MyCT Store Roles");
		}

		// filtering based on site
		Filter filter = booleanFilter.addTerm(Field.GROUP_ID, _configuration.GroupId());

		booleanFilter.add(filter, BooleanClauseOccur.MUST);
		booleanFilter.add(roleFilter, BooleanClauseOccur.MUST);
		booleanFilter.add(booleanFilter.addTerm(Field.ENTRY_CLASS_NAME, Publication.class.getName()),
				BooleanClauseOccur.MUST);

		BooleanQuery boolQuery = new BooleanQueryImpl();
		try {
			boolQuery.add(new WildcardQueryImpl("title", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
			boolQuery.add(new MatchQuery("title", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
		} catch (ParseException e) {
			e.printStackTrace();
			_log.warn(e);
		}

		boolQuery.setPostFilter(booleanFilter);
		Hits hits = IndexSearcherHelperUtil.search(searchContext,
				boolQuery); /* searching in title only */
		processHits(hits, typeAheads, "title");

		boolQuery = new BooleanQueryImpl();
		try {
			boolQuery.add(new WildcardQueryImpl("dealName", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
			boolQuery.add(new MatchQuery("dealName", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
		} catch (ParseException e) {
			e.printStackTrace();
			_log.warn(e);
		}
		boolQuery.setPostFilter(booleanFilter);
		hits = IndexSearcherHelperUtil.search(searchContext,
				boolQuery); /* searching in dealName only */
		processHits(hits, typeAheads, "dealName");

	}

	private List<TypeAhead> doPagination(List<TypeAhead> typeAheads) {
		if (_configuration.TypeAheadSuggstionLength() <= typeAheads.size())
			typeAheads = typeAheads.subList(0, _configuration.TypeAheadSuggstionLength());

		return typeAheads;

	}

	private void doSorting(List<TypeAhead> typeAheads) {

		Collections.sort(typeAheads);

	}

	/**
	 * Adds the value of FieldName and score from documents in hits into the
	 * typeAheads List.
	 * 
	 * @param hits
	 * @param typeAheads
	 * @param fieldName
	 */
	private void processHits(Hits hits, List<TypeAhead> typeAheads, String fieldName) {
		int i = 0;
		for (Document doc : hits.toList()) {

			TypeAhead temp = new TypeAhead();
			temp.setScore(hits.score(i));
			temp.setTitle(doc.get(fieldName));
			++i;
			typeAheads.add(temp);
		}

	}

	private void searchCTUTypeAheads(SearchContext searchContext, List<TypeAhead> typeAheads) throws PortalException {
		User user = UserLocalServiceUtil.getUser(searchContext.getUserId());

		/**
		 * 
		 * AccessLevel 0 MyCT Store Staff Role. AccessLevel 1 MyCT General
		 * Manager Role. AccessLevel 2 MyCT Senior Manager Role. AccessLevel 3
		 * MyCT Dealer Role.
		 */

		List<Role> user_roles = user.getRoles(); // get user level
													// roles

		// reading all inherited roles of user from the user_groups
		List<UserGroup> user_groups = user.getUserGroups();

		for (UserGroup ug : user_groups) {
			user_roles.addAll(RoleLocalServiceUtil.getGroupRoles(ug.getGroupId()));
		}

		BooleanFilter booleanFilter = new BooleanFilter();
		BooleanFilter accessFilter = new BooleanFilter();

		/*
		 * filtering based on 'accessLevel' field in CTU with respect to user
		 * role
		 */
		for (Role role : user_roles) {

			if ((boolean) role.getExpandoBridge().getAttribute(FieldName.MyCTStoreRoleExpandoFieldName) == true) {

				Filter filter = null;
				if (role.getName().contentEquals(FieldName.DealerRoleName)) {

					filter = accessFilter.addTerm(ReadConfig.getCTUAccessFieldName(), 3);

					accessFilter.add(filter, BooleanClauseOccur.SHOULD);
				}
				if (role.getName().contentEquals(FieldName.SeniorManagerRoleName)) {

					filter = accessFilter.addTerm(ReadConfig.getCTUAccessFieldName(), 2);

					accessFilter.add(filter, BooleanClauseOccur.SHOULD);
				}
				if (role.getName().contentEquals(FieldName.ManagerRoleName)) {

					filter = accessFilter.addTerm(ReadConfig.getCTUAccessFieldName(), 1);

					accessFilter.add(filter, BooleanClauseOccur.SHOULD);
				}
				if (role.getName().contentEquals(FieldName.StaffRoleName)) {

					filter = accessFilter.addTerm(ReadConfig.getCTUAccessFieldName(), 0);

					accessFilter.add(filter, BooleanClauseOccur.SHOULD);
				}
			}

		}

		booleanFilter.add(accessFilter, BooleanClauseOccur.MUST);

		// filtering based on site D
		Filter filter = booleanFilter.addTerm(Field.GROUP_ID, _configuration.GroupId());
		booleanFilter.add(filter, BooleanClauseOccur.MUST);

		// filtering using ddm key
		filter = booleanFilter.addTerm("ddmStructureKey", _configuration.CTUDDMStructureKey());
		booleanFilter.add(filter, BooleanClauseOccur.SHOULD);

		booleanFilter.add(booleanFilter.addTerm(Field.ENTRY_CLASS_NAME, JournalArticle.class.getName()),
				BooleanClauseOccur.MUST);

		BooleanQuery boolQuery = new BooleanQueryImpl();
		try {
			boolQuery.add(new WildcardQueryImpl("title", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
			boolQuery.add(new MatchQuery("title", searchContext.getKeywords()), BooleanClauseOccur.SHOULD);
		} catch (ParseException e) {
			e.printStackTrace();
			_log.warn(e);
		}

		boolQuery.setPostFilter(booleanFilter);
		Hits hits = IndexSearcherHelperUtil.search(searchContext,
				boolQuery); /* searching in title only */
		processHits(hits, typeAheads, "title");

	}

	private static volatile MyCTSearchConfig _configuration;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		set_configuration(ConfigurableUtil.createConfigurable(MyCTSearchConfig.class, properties));
	}

	public static MyCTSearchConfig get_configuration() {
		return _configuration;
	}

	public static void set_configuration(MyCTSearchConfig _configuration) {
		TypeAheadResource._configuration = _configuration;
	}

}