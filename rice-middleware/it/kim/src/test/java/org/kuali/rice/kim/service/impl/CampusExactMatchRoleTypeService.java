/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
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
package org.kuali.rice.kim.service.impl;

import java.util.Collections;
import java.util.List;

import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.location.impl.campus.CampusRoleTypeServiceImpl;

public class CampusExactMatchRoleTypeService extends CampusRoleTypeServiceImpl {

    @Override
    public List<String> getQualifiersForExactMatch() {
        return Collections.singletonList(KimConstants.AttributeConstants.CAMPUS_CODE);
    }

}
