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
package org.kuali.rice.kns.lookup;

import java.util.List;

import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.kns.util.GlobalVariables;

/**
 * This is a description of what this class does - kellerj don't forget to fill this in.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class ParameterLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    /**
     * Hides the edit/copy links when not valid for the current user.
     *
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getCustomActionUrls(org.kuali.rice.kns.bo.BusinessObject, java.lang.List)
     */
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        Parameter parm = (Parameter)businessObject;
        if ( GlobalVariables.getUserSession().getPerson().isMember( parm.getParameterWorkgroupName() ) ) {
            return super.getCustomActionUrls(businessObject, pkNames);
        } else {
            return super.getEmptyActionUrls();
        }
    }
}

