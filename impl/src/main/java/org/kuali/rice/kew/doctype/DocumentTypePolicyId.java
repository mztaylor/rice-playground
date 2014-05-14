/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.rice.kew.doctype;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * This Compound Primary Class has been generated by the rice ojb2jpa Groovy script.  Please
 * note that there are no setter methods, only getters.  This is done purposefully as cpk classes
 * can not change after they have been created.  Also note they require a public no-arg constructor.
 * TODO: Implement the equals() and hashCode() methods. 
 */
public class DocumentTypePolicyId implements Serializable {

    @Column(name="DOC_TYP_ID")
    private Long documentTypeId;
    @Column(name="DOC_PLCY_NM")
    private String policyName;

    public DocumentTypePolicyId() {}

    public Long getDocumentTypeId() { return documentTypeId; }

    public String getPolicyName() { return policyName; }

	/**
	 * This overridden method ...
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.documentTypeId == null) ? 0 : this.documentTypeId
						.hashCode());
		result = prime * result
				+ ((this.policyName == null) ? 0 : this.policyName.hashCode());
		return result;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DocumentTypePolicyId other = (DocumentTypePolicyId) obj;
		if (this.documentTypeId == null) {
			if (other.documentTypeId != null)
				return false;
		} else if (!this.documentTypeId.equals(other.documentTypeId))
			return false;
		if (this.policyName == null) {
			if (other.policyName != null)
				return false;
		} else if (!this.policyName.equals(other.policyName))
			return false;
		return true;
	}

/*
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DocumentTypePolicyId)) return false;
        if (o == null) return false;
        DocumentTypePolicyId pk = (DocumentTypePolicyId) o;
        // TODO: Finish implementing this method.  Compare o to pk and return true or false.
        throw new UnsupportedOperationException("Please implement me!");
    }

    public int hashCode() {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Please implement me!");
    }
*/    

}
