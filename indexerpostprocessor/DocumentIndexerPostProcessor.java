
package com.ctc.myct.search.indexerpostprocessor;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.ctc.myct.search.configuration.MyCTSearchConfig;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Vishnu
 *
 */
@Component(configurationPid = "com.ctc.myct.search.configuration.MyCTSearchConfig", immediate = true, service = IndexerPostProcessor.class, property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry")
public class DocumentIndexerPostProcessor implements IndexerPostProcessor {
	private static volatile MyCTSearchConfig _configuration;
	private static final Log _log = LogFactoryUtil.getLog(DocumentIndexerPostProcessor.class);

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_configuration = ConfigurableUtil.createConfigurable(MyCTSearchConfig.class, properties);
	}

	@Override
	public void postProcessContextBooleanFilter(BooleanFilter booleanFilter, SearchContext searchContext)
			throws Exception {
		if (ValidateSiteId(searchContext)) {
			long userId = searchContext.getUserId();
			User user = UserLocalServiceUtil.getUser(userId);
			BooleanFilter orgFilter = new BooleanFilter();

			if (user.getOrganizations().size() == 0) {

				// if user has no organizations, then no results
				Filter filter = orgFilter.addTerm("organizationId", -1);

				orgFilter.add(filter, BooleanClauseOccur.SHOULD);
			} else {
				for (Organization org : user.getOrganizations()) {
					Filter filter = orgFilter.addTerm("organizationId", org.getOrganizationId());

					orgFilter.add(filter, BooleanClauseOccur.SHOULD);
				}
			}

			booleanFilter.add(orgFilter, BooleanClauseOccur.MUST);

			// filtering Site ID
			Filter sitefilter = booleanFilter.addTerm(Field.GROUP_ID, _configuration.GroupId());

			booleanFilter.add(sitefilter, BooleanClauseOccur.MUST);
		}
	}

	@Override
	public void postProcessContextQuery(BooleanQuery contextQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessDocument(Document document, Object obj) throws Exception {
		if (Long.parseLong(document.get(Field.GROUP_ID)) == _configuration.GroupId()) {
			try {
				String orgId = document.get("expando__keyword__custom_fields__organizationId");

				if (Validator.isNotNull(orgId))
					document.addKeyword("organizationId", orgId);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	@Override
	public void postProcessFullQuery(BooleanQuery fullQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessSearchQuery(BooleanQuery searchQuery, SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessSearchQuery(BooleanQuery searchQuery, BooleanFilter booleanFilter,
			SearchContext searchContext) throws Exception {
	}

	@Override
	public void postProcessSummary(Summary summary, Document document, Locale locale, String snippet) {
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
