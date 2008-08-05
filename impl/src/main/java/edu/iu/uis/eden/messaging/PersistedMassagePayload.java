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
package edu.iu.uis.eden.messaging;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.rice.ksb.services.KSBServiceLocator;

/**
 * Holds message payload content.  Needed to proxy the content so we don't have to 
 * take the hit when grabbing large amounts of persisted messages at time.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
@Entity
@Table(name="EN_MSG_PAYLOAD_T")
public class PersistedMassagePayload implements Serializable {
    
    private static final long serialVersionUID = 508778527504899029L;
    
    @Id
	@Column(name="MESSAGE_QUE_ID")
	private Long routeQueueId;
    @Lob
	@Basic(fetch=FetchType.LAZY)
	@Column(name="MESSAGE_PAYLOAD")
	private String payload;
    @Transient
    private AsynchronousCall methodCall;
    @Transient
    private PersistedMessage message;
    
    public PersistedMassagePayload() {}
    
    public PersistedMassagePayload (AsynchronousCall methodCall, PersistedMessage message) {
	this.setPayload(KSBServiceLocator.getMessageHelper().serializeObject(methodCall));
	this.methodCall = methodCall;
	this.message = message;
    }
    
    public String getPayload() {
        return this.payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    public Long getRouteQueueId() {
        return this.routeQueueId;
    }
    public void setRouteQueueId(Long routeQueueId) {
        this.routeQueueId = routeQueueId;
    }
    public AsynchronousCall getMethodCall() {
	if (this.methodCall != null) {
	    return this.methodCall;
	} 
	this.methodCall = (AsynchronousCall)KSBServiceLocator.getMessageHelper().deserializeObject(getPayload());
	return this.methodCall;
    }

    public PersistedMessage getMessage() {
        return this.message;
    }

    public void setMessage(PersistedMessage message) {
        this.message = message;
    }

    public void setMethodCall(AsynchronousCall methodCall) {
        this.methodCall = methodCall;
    }
    

}

