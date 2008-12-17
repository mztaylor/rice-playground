/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.rice.kns.service;

import java.util.Map;
import java.util.Set;

import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;

/**
 * The DocumentAuthorizer class associated with a given Document is used to dynamically determine what editing mode and what actions
 * are allowed for a given user on a given document instance.
 * 
 * 
 */
public interface DocumentPessimisticLockerService {
  
    /**
     * @param document - the document locks are to be established against or by
     * @param editMode - the editMode returned by the method {@link #getEditMode(Document, Person)}
     * @param user - the user locks are being established for
     * @return New map generated by locking logic combined with passed in parameter editMode.  Map contains keys 
     *         AuthorizationConstants.EditMode value (String) which indicates what operations the user is currently 
     *         allowed to take on that document.  This may be a modified list of 
     */
    public Map establishLocks(Document document, Map editMode, Person user);

    /**
     * @param document - the document to create the lock against and add the lock to
     */
    public void establishWorkflowPessimisticLocking(Document document);

    /**
     * @param document - document to release locks from
     */
    public void releaseWorkflowPessimisticLocking(Document document);
    
    /**
     * @param document
     * @param user
     * @return Set of actions are permitted the given user on the given document
     */
    public Set getDocumentActions(Document document, Person user, Set<String> documentActions);
  
}

