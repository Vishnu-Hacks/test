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
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;

@Component(configurationPid = "com.ctc.myct.search.configuration.MyCTSearchConfig", immediate = true, service = IndexerPostProcessor.class, property = "indexer.class.name=com.liferay.journal.model.JournalArticle")
public class WebContentIndexerPostProcessor implements IndexerPostProcessor {
	private static final Log _log = LogFactoryUtil.getLog(WebContentIndexerPostProcessor.class);
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

			String content = (String) searchContext.getAttribute("content");

			if (Validator.isNotNull(content)) {
				content = content.trim().toLowerCase();

				if (content.contains("rsrt")) {
					_log.info("Filtering RSRT content using structre key : " + _configuration.RSRTDDMStructureKey());

					Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.RSRTDDMStructureKey());

					booleanFilter.add(filter, BooleanClauseOccur.MUST);
				} else if (content.contains("bestbets")) {
					_log.info("Filtering Best Bets content using structre key : "
							+ _configuration.BestBetsDDMStructureKey());

					Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.BestBetsDDMStructureKey());

					booleanFilter.add(filter, BooleanClauseOccur.MUST);
				} else if (content.contains("vimeo")) {
					_log.info("Filtering Vimeo content using structre key : " + _configuration.VimeoDDMStructureKey());

					Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.VimeoDDMStructureKey());

					booleanFilter.add(filter, BooleanClauseOccur.MUST);
				} else if (content.contains("techtraining")) {
					_log.info("Filtering Tech Training content using structre key : "
							+ _configuration.TECHTRAININGStructureKey());

					Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.TECHTRAININGStructureKey());

					booleanFilter.add(filter, BooleanClauseOccur.MUST);
				} else if (content.contains("ctu")) {
					_log.info("Filtering CTU content using structre key : " + _configuration.CTUDDMStructureKey());

					Filter filter = booleanFilter.addTerm("ddmStructureKey", _configuration.CTUDDMStructureKey());

					booleanFilter.add(filter, BooleanClauseOccur.MUST);
					_log.info("Filtering CTU content using 'accessLevel' field");

					User user = UserLocalServiceUtil.getUser(searchContext.getUserId());

					/**
					 * 
					 * AccessLevel 0 MyCT Store Staff Role. AccessLevel 1 MyCT
					 * General Manager Role. AccessLevel 2 MyCT Senior Manager
					 * Role. AccessLevel 3 MyCT Dealer Role.
					 */

					List<Role> user_roles = user.getRoles(); // get user level
																// roles

					// reading all inherited roles of user from the user_groups
					List<UserGroup> user_groups = user.getUserGroups();

					for (UserGroup ug : user_groups) {
						user_roles.addAll(RoleLocalServiceUtil.getGroupRoles(ug.getGroupId()));
					}

					BooleanFilter accessFilter = new BooleanFilter();

					for (Role role : user_roles) {

						if ((boolean) role.getExpandoBridge()
								.getAttribute(FieldName.MyCTStoreRoleExpandoFieldName) == true) {

							if (role.getName().contentEquals(FieldName.DealerRoleName)) {

								_log.info("User=dealer;Filtering CTU documents with 'accessLevel' field  : " + 3);

								filter = accessFilter.addTerm("accessLevel", 3);

								accessFilter.add(filter, BooleanClauseOccur.SHOULD);
							}
							if (role.getName().contentEquals(FieldName.SeniorManagerRoleName)) {

								_log.info(
										"User=senior-manager;Filtering CTU documents with 'accessLevel' field  : " + 2);

								filter = accessFilter.addTerm("accessLevel", 2);

								accessFilter.add(filter, BooleanClauseOccur.SHOULD);
							}
							if (role.getName().contentEquals(FieldName.ManagerRoleName)) {

								_log.info("User=manager;Filtering CTU documents with 'accessLevel' field  : " + 1);

								filter = accessFilter.addTerm("accessLevel", 1);

								accessFilter.add(filter, BooleanClauseOccur.SHOULD);
							}
							if (role.getName().contentEquals(FieldName.StaffRoleName)) {

								_log.info("User=staff;Filtering CTU documents with 'accessLevel' field : " + 0);

								filter = accessFilter.addTerm("accessLevel", 0);

								accessFilter.add(filter, BooleanClauseOccur.SHOULD);
							}
						}

					}

					booleanFilter.add(accessFilter, BooleanClauseOccur.MUST);

				}
			}

			// filtering based on site D
			Filter filter = booleanFilter.addTerm(Field.GROUP_ID, _configuration.GroupId());

			booleanFilter.add(filter, BooleanClauseOccur.MUST);
		}
	}

	@Override
	public void postProcessContextQuery(BooleanQuery contextQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessDocument(Document document, Object obj) throws Exception {
		if (Long.parseLong(document.get(Field.GROUP_ID)) == _configuration.GroupId()) {

			String publication = document.get("expando__keyword__custom_fields__publicationId");

			/**
			 * Replacing DDM field of Journal Articles that contains
			 * PublicationId.
			 */
			if (Validator.isNotNull(publication)) {
				document.addKeyword("publicationId", publication);
			}
			/**
			 * Replacing DDM field of CTU documents with more relevant name
			 */
			String accessLevelField = document.get("expando__keyword__custom_fields__accessLevel");
			if (Validator.isNotNull(accessLevelField)) {
				document.addKeyword("accessLevel", accessLevelField);
			}
		}
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
				// note.:. These fields must be indexed in ELS prior to search.
				String[] fields = { "title", "id", "channel", "description", "tags", "url", "articleId", "title",
						"keywords", "publication", "content", "type", "lessonCode", "primarySubject",
						"secondarySubject", "URL" };
				Map<String, Query> query = searchQuery.addTerms(fields, searchContext.getKeywords());

				for (String s : fields) {
					searchQuery.add(query.get(s), BooleanClauseOccur.SHOULD);
				}

				// Adding Search Weights based on Field.
				HashMap<Integer, String> fieldWeight = new HashMap<>();

				/**
				 * >> Field ranking for search. 1. Deals 2. Title 5.
				 * Synopsis/Description 6. Content 3. Keywords 4. Product
				 * Numbers 9. Section Name 8. File Name 7.File Wrapper Title
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
		}
	}

	@Override
	public void postProcessSummary(Summary summary, Document document, Locale locale, String snippet) {
	}

	public static void set_configuration(MyCTSearchConfig _configuration) {
		WebContentIndexerPostProcessor._configuration = _configuration;
	}

	Boolean ValidateSiteId(SearchContext context) {

		if (context.getGroupIds()[0] == _configuration.GroupId()) {
			_log.debug("Validating SiteID: true");
			return true;
		}
		_log.debug("Validating SiteID: false");
		return false;
	}
}