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
package edu.iu.uis.eden.doctype;

import java.io.Serializable;
import java.util.List;

import edu.iu.uis.eden.user.WorkflowUser;
import edu.iu.uis.eden.web.session.Authentication;

/**
 * This is an attribute used for document security and based off document type. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public interface SecurityAttribute extends Serializable {

    /**
     * This method ...
     * 
     * @param security
     * @param currentUser
     * @param authentications
     * @param docTypeName
     * @param documentId
     * @param initiatorWorkflowId
     * @param session
     * @return
     */
    public Boolean docSearchAuthorized(DocumentTypeSecurity security, WorkflowUser currentUser, List<Authentication> authentications, String docTypeName, Long documentId, String initiatorWorkflowId, SecuritySession session);

    /**
     * This method ...
     * 
     * @param security
     * @param currentUser
     * @param authentications
     * @param docTypeName
     * @param documentId
     * @param initiatorWorkflowId
     * @param session
     * @return
     */
    public Boolean routeLogAuthorized(DocumentTypeSecurity security, WorkflowUser currentUser, List<Authentication> authentications, String docTypeName, Long documentId, String initiatorWorkflowId, SecuritySession session);

}
