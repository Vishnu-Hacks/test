package com.ctc.myct.search.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "MyCT Search", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(factory = true, id = "com.ctc.myct.search.configuration.MyCTSearchConfig", localization = "content/Language", name = "MyCT Search Configuration")
public interface MyCTSearchConfig {
	@Meta.AD(deflt = "DDMStructureKey", required = true, description = "Structure Key for CTU content", name = "CTU Structure Key")
	String CTUDDMStructureKey();

	@Meta.AD(deflt = "0", required = true, description = "Site Id for MyCT", name = "Site Id")
	long GroupId();

	@Meta.AD(deflt = "DDMStructureKey", required = true, description = "Structure Key for RSRT content", name = "RSTR Structure Key")
	String RSRTDDMStructureKey();

	@Meta.AD(deflt = "DDMStructureKey", required = true, description = "Structure Key for Tech Training content", name = "Tech Training Structure Key")
	String TECHTRAININGStructureKey();

	@Meta.AD(deflt = "10", required = true, description = "Number of Suggestions required in type ahead search", name = "Type-Ahead Suggestions Length")
	int TypeAheadSuggstionLength();

	@Meta.AD(deflt = "DDMStructureKey", required = true, description = "Structure Key for Vimeo content", name = "Vimeo Structure Key")
	String VimeoDDMStructureKey();

	@Meta.AD(deflt = "DDMStructureKey", required = true, description = "Structure Key for Best Bets content", name = "Best Bets Structure Key")
	String BestBetsDDMStructureKey();
	
	@Meta.AD(deflt = "0", required = true, description = "Class Type Id for CTU structure", name = "CTU Class Type ID")
	String CTUClassTypeId();
}