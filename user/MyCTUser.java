package com.ctc.myct.search.user;

import java.util.List;

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;

public class MyCTUser {
    private String      emailAddress;
    private String      screenName;
    private long        id;
    private boolean     active;
    private boolean     defaultUser;
    private List<Group> group;
    private List<Role>  role;
    private long        companyID;

    public MyCTUser(User user) {
        this.emailAddress = user.getEmailAddress();
        this.screenName   = user.getScreenName();
        this.id           = user.getUserId();
        this.active       = user.isActive();
        this.defaultUser  = user.isDefaultUser();
        this.role         = user.getRoles();
        this.group        = user.getGroups();
        this.companyID    = user.getCompanyId();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCompanyID() {
        return companyID;
    }

    public void setCompanyID(long companyID) {
        this.companyID = companyID;
    }

    public boolean isDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(boolean defaultUser) {
        this.defaultUser = defaultUser;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<Group> getGroup() {
        return group;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Role> getRole() {
        return role;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
