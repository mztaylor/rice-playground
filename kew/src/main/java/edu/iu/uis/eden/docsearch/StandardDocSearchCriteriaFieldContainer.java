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
package edu.iu.uis.eden.docsearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.iu.uis.eden.util.Utilities;

/**
 * This class is a holder for fields for standard document search criteria objects
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class StandardDocSearchCriteriaFieldContainer implements java.io.Serializable {

	private static final long serialVersionUID = 7334865718915897486L;
	
	private String fieldKey;
	private String labelMessageKey;
	private String labelFieldWidthValue;
	private String labelFieldHeightValue;
	private String dataFieldWidthValue;
	private String dataFieldHeightValue;
	private List<StandardSearchCriteriaField> fields = new ArrayList<StandardSearchCriteriaField>();
	
	public StandardDocSearchCriteriaFieldContainer() {}
	
	public StandardDocSearchCriteriaFieldContainer(String labelKey, StandardSearchCriteriaField field) {
		if (!Utilities.isEmpty(labelKey)) {
			this.labelMessageKey = labelKey;
		}
		this.fieldKey = field.getKey();
		addField(field);
	}

	public StandardDocSearchCriteriaFieldContainer(String fieldKey, String labelKey, List<StandardSearchCriteriaField> fields) {
		if (!Utilities.isEmpty(labelKey)) {
			this.labelMessageKey = labelKey;
		}
		if (!Utilities.isEmpty(fieldKey)) {
			this.fieldKey = fieldKey;
		}
		this.fields = fields;
	}

	public StandardDocSearchCriteriaFieldContainer(StandardDocSearchCriteriaFieldContainer sourceContainer) {
		this.fieldKey = sourceContainer.getFieldKey();
		this.labelMessageKey = sourceContainer.getLabelMessageKey();
		this.labelFieldHeightValue = sourceContainer.getLabelFieldHeightValue();
		this.labelFieldWidthValue = sourceContainer.getLabelFieldWidthValue();
		this.dataFieldHeightValue = sourceContainer.getDataFieldHeightValue();
		this.dataFieldWidthValue = sourceContainer.getDataFieldWidthValue();
		this.fields = sourceContainer.getFields();
	}

	public void addField(StandardSearchCriteriaField field) {
		fields.add(field);
	}

    public boolean isHidden() {
        for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
            StandardSearchCriteriaField field = (StandardSearchCriteriaField) iterator.next();
            if (!field.isHidden()) {
                return false;
            }
        }
        return true;
    }
    
    public void hideFieldsIfNecessary(Set<String> hiddenFieldKeys) {
        for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
            StandardSearchCriteriaField field = (StandardSearchCriteriaField) iterator.next();
            if ( (hiddenFieldKeys.contains(getFieldKey())) || (hiddenFieldKeys.contains(field.getKey())) ) {
                field.setHidden(true);
            }
        }
    }

	public String getFieldKey() {
		return this.fieldKey;
	}

	public void setFieldKey(String fieldKey) {
		this.fieldKey = fieldKey;
	}

	public String getLabelMessageKey() {
		return this.labelMessageKey;
	}

	public void setLabelMessageKey(String labelMessageKey) {
		this.labelMessageKey = labelMessageKey;
	}

	public String getLabelFieldWidthValue() {
		return this.labelFieldWidthValue;
	}

	public void setLabelFieldWidthValue(String labelFieldWidthValue) {
		this.labelFieldWidthValue = labelFieldWidthValue;
	}

	public String getLabelFieldHeightValue() {
		return this.labelFieldHeightValue;
	}

	public void setLabelFieldHeightValue(String labelFieldHeightValue) {
		this.labelFieldHeightValue = labelFieldHeightValue;
	}

	public String getDataFieldWidthValue() {
		return this.dataFieldWidthValue;
	}

	public void setDataFieldWidthValue(String dataFieldWidthValue) {
		this.dataFieldWidthValue = dataFieldWidthValue;
	}

	public String getDataFieldHeightValue() {
		return this.dataFieldHeightValue;
	}

	public void setDataFieldHeightValue(String dataFieldHeightValue) {
		this.dataFieldHeightValue = dataFieldHeightValue;
	}

	public List<StandardSearchCriteriaField> getFields() {
		return this.fields;
	}

	public void setFields(List<StandardSearchCriteriaField> fields) {
		this.fields = fields;
	}
	
}
