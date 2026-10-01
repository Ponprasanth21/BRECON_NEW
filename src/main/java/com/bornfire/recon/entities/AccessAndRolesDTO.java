package com.bornfire.recon.entities;

import java.util.List;

public class AccessAndRolesDTO {
    private List<ACCESS_AND_ROLES_TEMP_ENTITY> tempRoles;
    private List<ACCESS_AND_ROLES_ENTITY> mainRoles;

    public AccessAndRolesDTO(List<ACCESS_AND_ROLES_TEMP_ENTITY> tempRoles,
                             List<ACCESS_AND_ROLES_ENTITY> mainRoles) {
        this.tempRoles = tempRoles;
        this.mainRoles = mainRoles;
    }

    public List<ACCESS_AND_ROLES_TEMP_ENTITY> getTempRoles() {
        return tempRoles;
    }

    public void setTempRoles(List<ACCESS_AND_ROLES_TEMP_ENTITY> tempRoles) {
        this.tempRoles = tempRoles;
    }

    public List<ACCESS_AND_ROLES_ENTITY> getMainRoles() {
        return mainRoles;
    }

    public void setMainRoles(List<ACCESS_AND_ROLES_ENTITY> mainRoles) {
        this.mainRoles = mainRoles;
    }
}
