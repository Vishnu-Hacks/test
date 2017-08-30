package com.ctc.myct.search.indexerpostprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.ctc.myct.search.configuration.MyCTSearchConfig;
import com.ctc.myct.search.model.FieldName;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.generic.FuzzyQuery;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;

@Component(configurationPid = "com.ctc.myct.search.configuration.MyCTSearchConfig", immediate = true, service = IndexerPostProcessor.class, property = "indexer.class.name=com.ctc.myct.bo.model.Publication")
public class PublicationIndexerPostProcessor implements IndexerPostProcessor {
	private static final Log _log = LogFactoryUtil.getLog(PublicationIndexerPostProcessor.class);
	private static volatile MyCTSearchConfig _configuration;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		set_configuration(ConfigurableUtil.createConfigurable(MyCTSearchConfig.class, properties));
	}

	public static MyCTSearchConfig get_configuration() {
		return _configuration;
	}

	@Override
	public void postProcessContextBooleanFilter(BooleanFilter booleanFilter, SearchContext searchContext)
			throws Exception {
		if (ValidateSiteId(searchContext)) {
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
					if ((boolean) role.getExpandoBridge()
							.getAttribute(FieldName.MyCTStoreRoleExpandoFieldName) == true) {
						_log.info("Filtering Contents with Role : " + role.getName());

						Filter filter = roleFilter.addTerm(Field.ROLE_ID, role.getRoleId());

						roleFilter.add(filter, BooleanClauseOccur.SHOULD);
						hasMyCTStoreRole = true;
					}
				} catch (Exception e) {
					_log.warn("No Custom Field " + FieldName.MyCTStoreRoleExpandoFieldName + " found in role "
							+ role.getName());
					_log.error(e);

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
		}
	}

	@Override
	public void postProcessContextQuery(BooleanQuery contextQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessDocument(Document document, Object obj) throws Exception {
	}

	@Override
	public void postProcessFullQuery(BooleanQuery fullQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessSearchQuery(BooleanQuery searchQuery, SearchContext searchContext) throws Exception {
		if (ValidateSiteId(searchContext)) {
			if (!Validator.isNull(searchContext.getKeywords())) {

				// Fields that need to be searched, change this fields as per
				// client
				// requirements
				// note.:. These fields must be indexed prior to search.
				String[] fields = { "firstName", "additional_Information", "publicationId", "title", "keywords", "url",
						"articleId", "lastName", "roleId", "content", "synopsis", "dealId", "productNumber",
						"fileWrapperTitle" };
				Map<String, Query> query = searchQuery.addTerms(fields, searchContext.getKeywords());

				for (String s : fields) {
					searchQuery.add(query.get(s), BooleanClauseOccur.SHOULD);

					// adding fuzzy query
					FuzzyQuery fuzzy = new FuzzyQuery(s, searchContext.getKeywords());
					fuzzy.setMaxEdits(5);
					/*
					 * When querying text or keyword fields, fuzziness is
					 * interpreted as a Levenshtein Edit Distance — the number
					 * of one character changes that need to be made to one
					 * string to make it the same as another string.
					 */

					fuzzy.setMaxExpansions(25);
					/*
					 * The maximum number of terms that the fuzzy query will
					 * expand to. Defaults to 50.
					 */
					fuzzy.setPrefixLength(0);
					/*
					 * The number of initial characters which will not be
					 * “fuzzified”. This helps to reduce the number of terms
					 * which must be examined. Defaults to 0.
					 */
					searchQuery.add(fuzzy, BooleanClauseOccur.SHOULD);

				}

			}
		}
	}

	@Override
	public void postProcessSearchQuery(BooleanQuery searchQuery, BooleanFilter fullQueryBooleanFilter,
			SearchContext searchContext) throws Exception {
		if (ValidateSiteId(searchContext)) {
			QueryConfig config = searchContext.getQueryConfig();

			config.setCollatedSpellCheckResultEnabled(true);
			config.setCollatedSpellCheckResultScoresThreshold(10);
			config.setHitsProcessingEnabled(true);
			config.setScoreEnabled(true);
			searchContext.setQueryConfig(config);

			// Adding Search Weights based on Field.
			HashMap<Integer, String> fieldWeight = new HashMap<>();

			/**
			 * >> Field ranking for search. 1. Deals 2. Title 5.
			 * Synopsis/Description 6. Content 3. Keywords 4. Product Numbers 9.
			 * Section Name 8. File Name 7.File Wrapper Title
			 */
			fieldWeight.put(0, "dealId"); // dealId or dealName ? TODO
			fieldWeight.put(1, "title");
			fieldWeight.put(2, "keywords");
			fieldWeight.put(3, "productNumber"); // ? TODO
			fieldWeight.put(4, "description");
			fieldWeight.put(5, "content");
			fieldWeight.put(6, "fileWrapperTitle"); // ? TODO
			fieldWeight.put(7, "fileName");
			fieldWeight.put(8, "sectionName");

			Sort[] sorts = new Sort[fieldWeight.size()];
			int i = 0;

			for (String field : fieldWeight.values()) {
				sorts[i] = new Sort(field, Sort.SCORE_TYPE, false);
				++i;
			}

			// Sorts in succession by the criteria in each SortField.
			searchContext.setSorts(sorts);
		}
	}

	@Override
	public void postProcessSummary(Summary summary, Document document, Locale locale, String snippet) {
	}

	public static void set_configuration(MyCTSearchConfig _configuration) {
		PublicationIndexerPostProcessor._configuration = _configuration;
	}

	Boolean ValidateSiteId(SearchContext context) {

		if (context.getGroupIds()[0] == _configuration.GroupId()) {
			_log.info("Validating SiteID: true");
			return true;
		}
		_log.info("Validating SiteID: false");
		return false;
	}
}