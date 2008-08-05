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
package org.kuali.bus.security.credentials;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.kuali.rice.ksb.security.credentials.CasProxyTicketCredentialsSource;
import org.kuali.rice.ksb.security.credentials.UsernamePasswordCredentialsSource;
import org.kuali.rice.security.credentials.CredentialsSource;
import org.kuali.rice.security.credentials.CredentialsSourceFactory;
import org.kuali.rice.security.credentials.CredentialsSource.CredentialsType;

/**
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 * @since 0.9
 *
 */
public class CredentialsSourceFactoryTest extends TestCase {
	
	private CredentialsSourceFactory credentialsSourceFactory;

	@Override
	protected void setUp() throws Exception {
		final CredentialsSourceFactory credentialsSourceFactory = new CredentialsSourceFactory();
		final List<CredentialsSource> credentialsSources = Arrays.asList(new CredentialsSource[] {new UsernamePasswordCredentialsSource("test", "Test"), new CasProxyTicketCredentialsSource()}); 

		credentialsSourceFactory.setCredentialsSources(credentialsSources);
		credentialsSourceFactory.afterPropertiesSet();
		
		this.credentialsSourceFactory = credentialsSourceFactory;
		super.setUp();
	}
	
	public void testCredentialsSourceExistsUsernamePassword() {
		final CredentialsSource cs = this.credentialsSourceFactory.getCredentialsForType(CredentialsType.USERNAME_PASSWORD);
		assertNotNull(cs);
		assertEquals(CredentialsType.USERNAME_PASSWORD, cs.getSupportedCredentialsType());
	}

	public void testCredentialsSourceExistsCas() {
		final CredentialsSource cs = this.credentialsSourceFactory.getCredentialsForType(CredentialsType.CAS);
		assertNotNull(cs);
		assertEquals(CredentialsType.CAS, cs.getSupportedCredentialsType());
	}

	public void testCredentialsSourceNotExists() {
		final CredentialsSource cs = this.credentialsSourceFactory.getCredentialsForType(CredentialsType.JAAS);
		assertNull(cs);
	}
}
