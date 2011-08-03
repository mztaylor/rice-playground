/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.kim.util;

import org.kuali.rice.core.api.util.RiceConstants;


/**
 * This class is used to hold constants that are used when exposing services to the bus
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class KIMWebServiceConstants {

	public static final String MODULE_TARGET_NAMESPACE = RiceConstants.RICE_JAXWS_TARGET_NAMESPACE_BASE + "/kim";

	public static final class PermissionService {
		public static final String WEB_SERVICE_NAME = "permissionServiceSOAP";
		public static final String INTERFACE_CLASS = "org.kuali.rice.kim.service.PermissionService";
		public static final String WEB_SERVICE_PORT = "KimPermissionServicePort";
		
		private PermissionService() {
			throw new UnsupportedOperationException("do not call");
		}
	}

	public static final class PermissionUpdateService {
		public static final String WEB_SERVICE_NAME = "permissionUpdateServiceSOAP";
		public static final String INTERFACE_CLASS = "org.kuali.rice.kim.service.PermissionUpdateService";
		public static final String WEB_SERVICE_PORT = "KimPermissionUpdateServicePort";

		private PermissionUpdateService() {
			throw new UnsupportedOperationException("do not call");
		}
	}

	public static final class IdentityService {
		public static final String WEB_SERVICE_NAME = "identityServiceSOAP";
		public static final String INTERFACE_CLASS = "org.kuali.rice.kim.api.identity.IdentityService";
		public static final String WEB_SERVICE_PORT = "IdentityServicePort";
		
		private IdentityService() {
			throw new UnsupportedOperationException("do not call");
		}
	}

	private KIMWebServiceConstants() {
		throw new UnsupportedOperationException("do not call");
	}
}
