/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.ksb.api.bus;

import java.net.URL;

import javax.xml.namespace.QName;

import org.kuali.rice.core.api.security.credentials.CredentialsSource.CredentialsType;



/**
 * TODO ...!
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface ServiceDefinition {
	
	void validate();
	
	QName getServiceName();
	
	URL getEndpointUrl();
		
	Endpoint establishEndpoint();
	
	ClassLoader getServiceClassLoader();
	
	Object getService();
	
	String getServicePath();
	
	String getApplicationNamespace();

	String getServiceVersion();
	
	String getType();
		
	boolean isQueue();
	
	Integer getPriority();
	
	Integer getRetryAttempts();
	
	Long getMillisToLive();
	
	String getMessageExceptionHandler();
	
	Boolean getBusSecurity();
	
	CredentialsType getCredentialsType();
	
}
