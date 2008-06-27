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
package org.kuali.core.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.core.bo.BusinessObject;
import org.kuali.core.bo.user.UniversalUser;
import org.kuali.core.document.authorization.PessimisticLock;
import org.kuali.core.service.PessimisticLockService;
import org.kuali.core.util.BeanPropertyComparator;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.ObjectUtils;
import org.kuali.core.web.ui.Field;
import org.kuali.core.web.ui.Row;
import org.kuali.rice.KNSServiceLocator;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.KNSPropertyConstants;

/**
 * This class is the lookup helper for {@link org.kuali.core.document.authorization.PessimisticLock} objects 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class PessimisticLockLookupableHelperServiceImpl extends AbstractLookupableHelperServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PessimisticLockLookupableHelperServiceImpl.class);
    
    private static final long serialVersionUID = -5839142187907211804L;
    
    private List<Row> localRows;
    
    /**
     * Hides the applicable links when the PessimisticLock is not owned by the current user
     * 
     * @see org.kuali.core.lookup.AbstractLookupableHelperServiceImpl#getActionUrls(org.kuali.core.bo.BusinessObject)
     */
    @Override
    public String getActionUrls(BusinessObject businessObject) {
        PessimisticLock lock = (PessimisticLock)businessObject;
        if ( (lock.isOwnedByUser(GlobalVariables.getUserSession().getUniversalUser())) || (KNSServiceLocator.getPessimisticLockService().isPessimisticLockAdminUser(GlobalVariables.getUserSession().getUniversalUser())) ) {
            return getMaintenanceUrl(businessObject, KNSConstants.DELETE_METHOD);
        } else {
            return "";
        }
    }

    /**
     * This overridden method checks whether the user is an admin user according to {@link PessimisticLockService#isPessimisticLockAdminUser(UniversalUser)} and if the user is not an admin user the user field is set to Read Only and the lookup field
     * 
     * @see org.kuali.core.lookup.AbstractLookupableHelperServiceImpl#getRows()
     */
    @Override
    public List<Row> getRows() {
        UniversalUser currentUser = GlobalVariables.getUserSession().getUniversalUser();
        if (KNSServiceLocator.getPessimisticLockService().isPessimisticLockAdminUser(currentUser)) {
            return super.getRows();
        } else {
            if ( (ObjectUtils.isNull(localRows)) || localRows.isEmpty() ) {
                localRows = new ArrayList<Row>();
                // hide a field and forcibly set a field
                for (Iterator<Row> iterator = super.getRows().iterator(); iterator.hasNext();) {
                    Row row = (Row) iterator.next();
                    boolean addRow = true;
                    for (Iterator<Field> iterator2 = row.getFields().iterator(); iterator2.hasNext();) {
                        Field field = (Field) iterator2.next();
                        if ((KNSPropertyConstants.OWNED_BY_USER + "." + KNSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER).equals(field.getPropertyName())) {
                            addRow = false;
                            break;
                        }
                    }
                    if (addRow) {
                        localRows.add(row);
                    }
                }
            }
            return localRows;
        }
    }

    /**
     * This method implementation is used to search for objects
     * 
     * @see org.kuali.core.lookup.AbstractLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        // remove hidden fields
        LookupUtils.removeHiddenCriteriaFields( getBusinessObjectClass(), fieldValues );
        // force criteria if not admin user
        UniversalUser currentUser = GlobalVariables.getUserSession().getUniversalUser();
        if (!KNSServiceLocator.getPessimisticLockService().isPessimisticLockAdminUser(currentUser)) {
            fieldValues.put(KNSPropertyConstants.OWNED_BY_PERSON_UNIVERSAL_ID,GlobalVariables.getUserSession().getUniversalUser().getPersonUniversalIdentifier());
        }

        setBackLocation(fieldValues.get(KNSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KNSConstants.DOC_FORM_KEY));
        setReferencesToRefresh(fieldValues.get(KNSConstants.REFERENCES_TO_REFRESH));
        LOG.info("Search Criteria: " + fieldValues);
        List searchResults;
        searchResults = (List) getLookupService().findCollectionBySearchHelper(getBusinessObjectClass(), fieldValues, true);
        // sort list if default sort column given
        List defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            Collections.sort(searchResults, new BeanPropertyComparator(getDefaultSortColumns(), true));
        }
        return searchResults;
    }
    
}
