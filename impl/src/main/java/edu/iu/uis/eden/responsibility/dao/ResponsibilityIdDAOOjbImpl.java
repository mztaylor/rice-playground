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
package edu.iu.uis.eden.responsibility.dao;

import org.apache.ojb.broker.PersistenceBroker;
import org.kuali.rice.core.resourceloader.GlobalResourceLoader;
import org.kuali.rice.database.platform.Platform;
import org.kuali.rice.util.RiceConstants;
import org.springmodules.orm.ojb.PersistenceBrokerCallback;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

public class ResponsibilityIdDAOOjbImpl extends PersistenceBrokerDaoSupport implements ResponsibilityIdDAO {

	public Long getNewResponsibilityId() {
        return (Long)this.getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            public Object doInPersistenceBroker(PersistenceBroker broker) {
            	return getPlatform().getNextValSQL("SEQ_RESPONSIBILITY_ID", broker);
            }
        });
    }

	protected Platform getPlatform() {
    	return (Platform)GlobalResourceLoader.getService(RiceConstants.DB_PLATFORM);
    }

}