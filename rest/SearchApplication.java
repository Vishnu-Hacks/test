package com.ctc.myct.search.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.ctc.myct.search.query.*;
import com.ctc.myct.search.user.UserResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import com.liferay.portal.kernel.service.UserLocalService;

/*
* MyCT Search application implementing a JAX-RS endpoint in DXP
 */
@Component(
    immediate           = true,
    service             = Application.class,
    configurationPid    = "com.ct.search.rest.SearchFilterConfiguration",
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    property            = { "jaxrs.application=true" }
)
@ApplicationPath("/search")
public class SearchApplication extends Application {
    private UserLocalService _userLocalService;

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();

        classes.add(new SearchResource().getClass());
        classes.add(new TypeAheadResource().getClass());

        return classes;
    }

    /*
     * Register our JAX-RS providers and resources
     */
    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<Object>();

        // add the automated Jackson marshaller for JSON
        singletons.add(new JacksonJsonProvider());

        // add  REST endpoints (resources)
        singletons.add(new UserResource(this));

        // singletons.add(new SearchResource());
        return singletons;
    }

    public UserLocalService getUserLocalService() {
        return this._userLocalService;
    }

    /*
     * Management of the Liferay UserLocalService class provided to us an OSGi service.
     */
    @Reference
    public void setUserLocalService(UserLocalService userLocalService) {
        this._userLocalService = userLocalService;
    }
}
