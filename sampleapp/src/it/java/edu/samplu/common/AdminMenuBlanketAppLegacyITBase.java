/**
 * Copyright 2005-2012 The Kuali Foundation
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
package edu.samplu.common;

import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;

/**
 * blanket approving a new document, results in a final document
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * @deprecated
 */
public abstract class AdminMenuBlanketAppLegacyITBase extends MenuLegacyITBase{

    /**
     * blanket approve document, returning docId as a String
     * @return docId
     */
//    public abstract String blanketApprove() throws Exception;

    @Override
    protected String getMenuLinkLocator() {
        return AdminMenuLegacyITBase.ADMIN_LOCATOR;
    }

    @Override
    protected String getCreateNewLinkLocator() {
        return AdminMenuLegacyITBase.CREATE_NEW_LOCATOR;
    }

//    @Test
//    public void testBlanketApprove() throws Exception {
//        gotoCreateNew();
//        String docId = blanketApprove();
//        blanketApproveTest();
//        assertDocFinal(docId);
//        passed();
//    }

    protected void assertBlanketApproveButtonsPresent() {
        assertElementPresentByName("methodToCall.route");
        assertElementPresentByName("methodToCall.save");
        assertElementPresentByName("methodToCall.blanketApprove", "Blanket Approve button not present does " + user + " have permssion?");
        assertElementPresentByName("methodToCall.close");
        assertElementPresentByName("methodToCall.cancel");
    }

    protected void assertDocFinal(String docId) throws InterruptedException {
        waitFor(By.linkText("spreadsheet"));
        if(isElementPresent(By.linkText(docId))){
            assertEquals("FINAL", getDocStatus());
        }else{
            assertEquals(docId, driver.findElement(By.xpath("//table[@id='row']/tbody/tr[1]/td[1]")));
            assertEquals("FINAL", getDocStatus());
        }
    }

    private String getDocStatus() {
        return driver.findElement(By.xpath("//table[@id='row']/tbody/tr[1]/td[4]")).getText();
    }
}