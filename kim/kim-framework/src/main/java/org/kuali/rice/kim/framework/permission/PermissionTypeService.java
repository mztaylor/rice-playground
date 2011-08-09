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
package org.kuali.rice.kim.framework.permission;

import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.kim.api.permission.Permission;
import org.kuali.rice.kim.framework.type.KimTypeService;

import java.util.List;
import java.util.Map;


/**
 * A {@link KimTypeService} with specific methods for Permissions.
 */
public interface PermissionTypeService extends KimTypeService {

    /**
     * Gets whether a permission assignment with the given details is applicable for the given request details.
     * 
     * For example, the details for a permission (say edit) could be as follows:
     *   component = Account
     *   field = incomeStreamAccountNumber
     *   
     * The Account component is known to belong to the KFS-COA namespace.  If this service is requested...
     * component = Account, field = All  
     *
     * @param requestedDetails the requested details.  cannot be null.
     * @param permissions the list of permission to check for matches. cannot be null.
     * @return an immutable list of matched permissions.  will not return null.
     * @throws IllegalArgumentException if the requestedDetails or permissions is null.
     */
    List<Permission> getMatchingPermissions( Map<String, String> requestedDetails, List<Permission> permissions) throws RiceIllegalArgumentException;
}
