/*
 * Copyright 2005-2007 The Kuali Foundation.
 * 
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
package edu.iu.uis.eden.engine;

import org.kuali.rice.kew.dto.WorkflowAttributeDefinitionDTO;

public class ChartOrgDispatchDefinition extends WorkflowAttributeDefinitionDTO {

    private static final long serialVersionUID = 1368857682079504618L;

    public ChartOrgDispatchDefinition(String workgroupName) {
        super(ChartOrgDispatchAttribute.class.getName());
        addConstructorParameter(workgroupName);
    }

}
