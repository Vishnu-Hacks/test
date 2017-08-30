package com.ctc.myct.search.configuration;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Component(configurationPid = "com.ctc.myct.search.configuration.MyCTSearchConfig")
public class ReadConfig {
	private static final Log _log = LogFactoryUtil.getLog(ReadConfig.class);
	private static volatile MyCTSearchConfig _configuration;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_log.info("Configurations Modified : " + properties);
		_configuration = ConfigurableUtil.createConfigurable(MyCTSearchConfig.class, properties);
	}

	public static long getSiteId() {
		return _configuration.GroupId();
	}

	public static int getTypeAheadLength() {
		return _configuration.TypeAheadSuggstionLength();
	}
	
	/**
	 * Creates field name created by the liferay to index 'accessLevel' field in CTU structure using 
	 * CTU class Type Id from OSGi configuration
	 * @return CTU accessLevel field name indexed in elastic
	 */
	public static String getCTUAccessFieldName(){
		
		return "ddm__keyword__"+_configuration.CTUClassTypeId()+"__accessLevel_en_US";
	}
}