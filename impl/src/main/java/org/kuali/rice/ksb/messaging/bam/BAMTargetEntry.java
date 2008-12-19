/*
 * Copyright 2005-2006 The Kuali Foundation.
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
package org.kuali.rice.ksb.messaging.bam;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.rice.core.jpa.annotations.Sequence;
import org.kuali.rice.core.util.OrmUtils;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.ksb.messaging.AsynchronousCallback;


/**
 * An entry in the BAM representing a service method invocation.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
@Entity
@Table(name="KRSB_BAM_T")
@Sequence(name="KRSB_BAM_S", property="bamId")
public class BAMTargetEntry implements Serializable {

	private static final long serialVersionUID = -8376674801367598316L;

	@Id
	@Column(name="BAM_ID")
	private Long bamId;
	@Column(name="SVC_NM")
	private String serviceName;
	@Column(name="MTHD_NM")
	private String methodName;
	@Column(name="THRD_NM")
	private String threadName;
	//@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CALL_DT")
	private Timestamp callDate;
	@Column(name="SVC_URL")
	private String serviceURL;
	@Column(name="TGT_TO_STR")
	private String targetToString;
	@Column(name="EXCPN_TO_STR")
	private String exceptionToString;
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Column(name="EXCPN_MSG")
	private String exceptionMessage;
	@Column(name="SRVR_IND")
	private Boolean serverInvocation;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="bamParamId")
	private List<BAMParam> bamParams = new ArrayList<BAMParam>();
	
	//for async calls not bam
	@Transient
	private AsynchronousCallback callback;
	
	@PrePersist
    public void beforeInsert(){
        OrmUtils.populateAutoIncValue(this, KNSServiceLocator.getEntityManagerFactory().createEntityManager());
    }
	
	public void addBamParam(BAMParam bamParam) {
		this.bamParams.add(bamParam);
	}
	public String getExceptionToString() {
		return this.exceptionToString;
	}
	public void setExceptionToString(String exceptionToString) {
		this.exceptionToString = exceptionToString;
	}
	public String getServiceName() {
		return this.serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceURL() {
		return this.serviceURL;
	}
	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}
	public String getTargetToString() {
		return this.targetToString;
	}
	public void setTargetToString(String targetToString) {
		this.targetToString = targetToString;
	}
	public Long getBamId() {
		return this.bamId;
	}
	public void setBamId(Long bamId) {
		this.bamId = bamId;
	}
	public String getExceptionMessage() {
		return this.exceptionMessage;
	}
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
	public Boolean getServerInvocation() {
		return this.serverInvocation;
	}
	public void setServerInvocation(Boolean clientInvocation) {
		this.serverInvocation = clientInvocation;
	}
	public Timestamp getCallDate() {
		return this.callDate;
	}
	public void setCallDate(Timestamp callDate) {
		this.callDate = callDate;
	}
	public String getMethodName() {
		return this.methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getThreadName() {
		return this.threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public List<BAMParam> getBamParams() {
		return this.bamParams;
	}
	public void setBamParams(List<BAMParam> bamParams) {
		this.bamParams = bamParams;
	}
	public AsynchronousCallback getCallback() {
		return this.callback;
	}
	public void setCallback(AsynchronousCallback callback) {
		this.callback = callback;
	}
}

