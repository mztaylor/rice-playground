/*
 * Copyright 2007-2008 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kns.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.impl.KimAttributes;
import org.kuali.rice.kim.bo.role.dto.KimPermissionInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.util.KimCommonUtils;

/**
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class DocumentTypeAndNodeOrStatePermissionTypeServiceImpl extends DocumentTypePermissionTypeServiceImpl {

	{
//		requiredAttributes.add(KimAttributes.ROUTE_NODE_NAME);
//		requiredAttributes.add(KimAttributes.ROUTE_STATUS_CODE);
	}

	/**
     *  Permission type service which can check the route node and status as well as the document hierarchy.
     *  
     *  Permission should be able to (in addition to taking the routingStatus, routingNote, and documentTypeName attributes) 
     *  should take a documentNumber and retrieve those values from workflow before performing the comparison.
     *
     *  consider the document type hierarchy - check for a permission that just specifies the document type first at each level 
     *  - then if you don't find that, check for the doc type and the node, then the doc type and the state. 
     * 
	 * @see org.kuali.rice.kns.service.impl.DocumentTypePermissionTypeServiceImpl#performPermissionMatches(org.kuali.rice.kim.bo.types.dto.AttributeSet, java.util.List)
	 */
    @Override
    protected List<KimPermissionInfo> performPermissionMatches(AttributeSet requestedDetails,
            List<KimPermissionInfo> permissionsList) {
        List<KimPermissionInfo> matchingPermissions = new ArrayList<KimPermissionInfo>();
        // loop over the permissions, checking the non-document-related ones
        for ( KimPermissionInfo kpi : permissionsList ) {
            if ( routeNodeMatches(requestedDetails, kpi.getDetails()) && 
                    routeStatusMatches(requestedDetails, kpi.getDetails())) {
                matchingPermissions.add( kpi );
            }           
        }
        // now, filter the list to just those for the current document
        matchingPermissions = super.performPermissionMatches( requestedDetails, matchingPermissions );
        return matchingPermissions;
    }
	
	protected boolean routeNodeMatches(AttributeSet requestedDetails, AttributeSet permissionDetails) {
		if ( StringUtils.isBlank( permissionDetails.get(KimAttributes.ROUTE_NODE_NAME) ) ) {
			return true;
		}
		return KimCommonUtils.isAttributeSetEntryEquals( requestedDetails, permissionDetails, 
				KimAttributes.ROUTE_NODE_NAME );
	}
	
	protected boolean routeStatusMatches(AttributeSet requestedDetails, AttributeSet permissionDetails) {
		if ( StringUtils.isBlank( permissionDetails.get(KimAttributes.ROUTE_STATUS_CODE) ) ) {
			return true;
		}
		return KimCommonUtils.isAttributeSetEntryEquals( requestedDetails, permissionDetails, 
				KimAttributes.ROUTE_STATUS_CODE );
	}

}