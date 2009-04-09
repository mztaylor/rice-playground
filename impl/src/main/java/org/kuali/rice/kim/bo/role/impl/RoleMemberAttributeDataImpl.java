/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.bo.role.impl;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.kuali.rice.kim.bo.types.impl.KimAttributeDataImpl;

/**
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
@Entity
@Table(name="KRIM_ROLE_MBR_ATTR_DATA_T")
public class RoleMemberAttributeDataImpl extends KimAttributeDataImpl {
    @Column(name="ROLE_MBR_ID")
    protected String roleMemberId;

    public String getRoleMemberId() {
        return this.roleMemberId;
    }

    public void setRoleMemberId(String groupId) {
        this.roleMemberId = roleMemberId;
    }

    /**
     * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put( "attrDataId", getAttributeDataId() );
        m.put( "permissionId", roleMemberId );
        m.put( "kimTypeId", getKimTypeId() );
        m.put( "kimAttributeId", getKimAttributeId() );
        m.put( "attributeValue", getAttributeValue() );
        return m;
    }
}
