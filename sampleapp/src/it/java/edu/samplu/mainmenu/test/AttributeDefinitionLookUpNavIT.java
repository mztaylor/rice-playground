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

package edu.samplu.mainmenu.test;

import org.junit.Test;
import org.openqa.selenium.By;

import edu.samplu.common.ITUtil;
import edu.samplu.common.MainMenuLookupLegacyITBase;
import edu.samplu.common.WebDriverLegacyITBase;


/**
 * tests whether the Attribute Definition Look UP is working ok 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AttributeDefinitionLookUpNavIT extends MainMenuLookupLegacyITBase{
    /**
     * This overridden method ...
     * 
     * @see edu.samplu.common.MenuLegacyITBase#getLinkLocator()
     */
    @Override
    protected String getLinkLocator() {
        // TODO dmoteria - THIS METHOD NEEDS JAVADOCS
        return "Attribute Definition Lookup";
    }
    
    @Test
    public void testAttributeDefinitionLookUp() throws Exception {
        gotoMenuLinkLocator();
        super.testAttributeDefinitionLookUp();
    }

   
}
