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
package org.kuali.rice.kim.service.impl;

import java.lang.ref.SoftReference;

/**
 * An extension to SoftReference that stores an expiration time for the 
 * value stored in the SoftReference. If no expiration time is passed in
 * the value will never be cached.  
 */
class MaxAgeSoftReference<T> extends SoftReference<T> {
	
	private long expires;

	public MaxAgeSoftReference(T referent) {
		super(referent);
	}
	
	public MaxAgeSoftReference(long expires, T referent) {
		super(referent);
		this.expires = System.currentTimeMillis() + expires * 1000;
	}
	
	public boolean isValid() {
		return System.currentTimeMillis() < expires;
	}
	
	public T get() {			
		return isValid() ? super.get() : null;
	}		
	
}