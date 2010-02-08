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

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.Role;
import org.kuali.rice.kim.bo.impl.RoleImpl;
import org.kuali.rice.kim.bo.role.dto.RoleMemberCompleteInfo;
import org.kuali.rice.kim.bo.role.impl.KimDelegationImpl;
import org.kuali.rice.kim.bo.role.impl.KimDelegationMemberAttributeDataImpl;
import org.kuali.rice.kim.bo.role.impl.KimDelegationMemberImpl;
import org.kuali.rice.kim.bo.role.impl.RoleMemberAttributeDataImpl;
import org.kuali.rice.kim.bo.role.impl.RoleMemberImpl;
import org.kuali.rice.kim.bo.role.impl.RoleResponsibilityActionImpl;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.bo.types.impl.KimAttributeImpl;
import org.kuali.rice.kim.service.RoleUpdateService;
import org.kuali.rice.kim.util.KIMWebServiceConstants;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.service.SequenceAccessorService;

/**
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@WebService(endpointInterface = KIMWebServiceConstants.RoleUpdateService.INTERFACE_CLASS, serviceName = KIMWebServiceConstants.RoleUpdateService.WEB_SERVICE_NAME, portName = KIMWebServiceConstants.RoleUpdateService.WEB_SERVICE_PORT, targetNamespace = KIMWebServiceConstants.MODULE_TARGET_NAMESPACE)
public class RoleUpdateServiceImpl extends RoleServiceBase implements RoleUpdateService {

	public void assignGroupToRole(String groupId, String namespaceCode, String roleName, AttributeSet qualifier) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	// check that identical member does not already exist
    	if ( doAnyMemberRecordsMatch( role.getMembers(), groupId, Role.GROUP_MEMBER_TYPE, qualifier ) ) {
    		return;
    	}
    	// create the new role member object
    	RoleMemberImpl newRoleMember = new RoleMemberImpl();
    	// get a new ID from the sequence
    	SequenceAccessorService sas = getSequenceAccessorService();
    	Long nextSeq = sas.getNextAvailableSequenceNumber(
    			KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S, RoleMemberImpl.class);
    	newRoleMember.setRoleMemberId( nextSeq.toString() );

    	newRoleMember.setRoleId( role.getRoleId() );
    	newRoleMember.setMemberId( groupId );
    	newRoleMember.setMemberTypeCode( Role.GROUP_MEMBER_TYPE );

    	// build role member attribute objects from the given AttributeSet
    	addMemberAttributeData( newRoleMember, qualifier, role.getKimTypeId() );

    	// When members are added to roles, clients must be notified.
    	getResponsibilityInternalService().saveRoleMember(newRoleMember);
    	getIdentityManagementNotificationService().roleUpdated();
    }

	public void assignPrincipalToRole(String principalId, String namespaceCode, String roleName, AttributeSet qualifier) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	role.refreshReferenceObject("members");
    	
    	// check that identical member does not already exist
    	if ( doAnyMemberRecordsMatch( role.getMembers(), principalId, Role.PRINCIPAL_MEMBER_TYPE, qualifier ) ) {
    		return;
    	}
    	// create the new role member object
    	RoleMemberImpl newRoleMember = new RoleMemberImpl();
    	// get a new ID from the sequence
    	SequenceAccessorService sas = getSequenceAccessorService();
    	Long nextSeq = sas.getNextAvailableSequenceNumber( 
    			KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S, RoleMemberImpl.class );    	
    	newRoleMember.setRoleMemberId( nextSeq.toString() );

    	newRoleMember.setRoleId( role.getRoleId() );
    	newRoleMember.setMemberId( principalId );
    	newRoleMember.setMemberTypeCode( Role.PRINCIPAL_MEMBER_TYPE );

    	// build role member attribute objects from the given AttributeSet
    	addMemberAttributeData( newRoleMember, qualifier, role.getKimTypeId() );

    	// add row to member table
    	// When members are added to roles, clients must be notified.
    	getResponsibilityInternalService().saveRoleMember(newRoleMember);
    	getIdentityManagementNotificationService().roleUpdated();
    }

	public void assignRoleToRole(String roleId, String namespaceCode, String roleName, AttributeSet qualifier) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	// check that identical member does not already exist
    	if ( doAnyMemberRecordsMatch( role.getMembers(), roleId, Role.ROLE_MEMBER_TYPE, qualifier ) ) {
    		return;
    	}
    	// create the new role member object
    	RoleMemberImpl newRoleMember = new RoleMemberImpl();
    	// get a new ID from the sequence
    	SequenceAccessorService sas = getSequenceAccessorService();
    	Long nextSeq = sas.getNextAvailableSequenceNumber(
    			KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S, RoleMemberImpl.class);
    	newRoleMember.setRoleMemberId( nextSeq.toString() );

    	newRoleMember.setRoleId( role.getRoleId() );
    	newRoleMember.setMemberId( roleId );
    	newRoleMember.setMemberTypeCode( Role.ROLE_MEMBER_TYPE );
    	// build role member attribute objects from the given AttributeSet
    	addMemberAttributeData( newRoleMember, qualifier, role.getKimTypeId() );

    	// When members are added to roles, clients must be notified.
    	getResponsibilityInternalService().saveRoleMember(newRoleMember);
    	getIdentityManagementNotificationService().roleUpdated();
    }

	public void removeGroupFromRole(String groupId, String namespaceCode, String roleName, AttributeSet qualifier) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	// pull all the group role members
    	// look for an exact qualifier match
		for ( RoleMemberImpl rm : role.getMembers() ) {
			if ( doesMemberMatch( rm, groupId, Role.GROUP_MEMBER_TYPE, qualifier ) ) {
		    	// if found, remove
				// When members are removed from roles, clients must be notified.
		    	getResponsibilityInternalService().removeRoleMember(rm);
			}
		}
		getIdentityManagementNotificationService().roleUpdated();
    }

	public void removePrincipalFromRole(String principalId, String namespaceCode, String roleName, AttributeSet qualifier ) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	// pull all the principal members
    	role.refreshReferenceObject("members");
    	// look for an exact qualifier match
		for ( RoleMemberImpl rm : role.getMembers() ) {
			if ( doesMemberMatch( rm, principalId, Role.PRINCIPAL_MEMBER_TYPE, qualifier ) ) {
		    	// if found, remove
				// When members are removed from roles, clients must be notified.
		    	getResponsibilityInternalService().removeRoleMember(rm);
			}
		}
		getIdentityManagementNotificationService().roleUpdated();
    }

	public void removeRoleFromRole(String roleId, String namespaceCode, String roleName, AttributeSet qualifier) {
    	// look up the role
    	RoleImpl role = getRoleImplByName( namespaceCode, roleName );
    	// pull all the group role members
    	// look for an exact qualifier match
		for ( RoleMemberImpl rm : role.getMembers() ) {
			if ( doesMemberMatch( rm, roleId, Role.ROLE_MEMBER_TYPE, qualifier ) ) {
		    	// if found, remove
				// When members are removed from roles, clients must be notified.
		    	getResponsibilityInternalService().removeRoleMember(rm);
			}
		}
		getIdentityManagementNotificationService().roleUpdated();
    }

	/**
     * 
     * This overridden method ...
     * 
     * @see org.kuali.rice.kim.service.RoleUpdateService#assignRoleAsDelegationMemberToRole(java.lang.String, java.lang.String, java.lang.String, org.kuali.rice.kim.bo.types.dto.AttributeSet)
     */
    public void saveDelegationMemberForRole(String delegationMemberId,
    			String roleMemberId, String memberId, String memberTypeCode, String delegationTypeCode, 
    			String roleId, AttributeSet qualifications, Date activeFromDate, Date activeToDate) throws UnsupportedOperationException{
    	if(StringUtils.isEmpty(delegationMemberId) && StringUtils.isEmpty(memberId) && StringUtils.isEmpty(roleId)){
    		throw new IllegalArgumentException("Either Delegation member ID or a combination of member ID and role ID must be passed in.");
    	}
    	// look up the role
    	RoleImpl role = getRoleImpl(roleId);
    	KimDelegationImpl delegation = getDelegationOfType(role.getRoleId(), delegationTypeCode);
    	// create the new role member object
    	KimDelegationMemberImpl newDelegationMember = new KimDelegationMemberImpl();

    	Map<String, String> criteria = new HashMap<String, String>();
    	KimDelegationMemberImpl origDelegationMember = null;
    	if(StringUtils.isNotEmpty(delegationMemberId)){
    		criteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMemberId);
	    	origDelegationMember = (KimDelegationMemberImpl)getBusinessObjectService().findByPrimaryKey(KimDelegationMemberImpl.class, criteria);
    	} else{
	    	criteria.put(KimConstants.PrimaryKeyConstants.MEMBER_ID, memberId);
	    	criteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegation.getDelegationId());
	    	List<KimDelegationMemberImpl> origDelegationMembers = (List<KimDelegationMemberImpl>)getBusinessObjectService().findMatching(KimDelegationMemberImpl.class, criteria);
	    	origDelegationMember = 
	    		(origDelegationMembers!=null && origDelegationMembers.size()>0) ? origDelegationMembers.get(0) : null;
    	}
    	if(origDelegationMember!=null){
    		newDelegationMember.setDelegationMemberId(origDelegationMember.getDelegationMemberId());
    		newDelegationMember.setVersionNumber(origDelegationMember.getVersionNumber());
    	} else{
    		newDelegationMember.setDelegationMemberId(getNewDelegationMemberId());
    	}
    	newDelegationMember.setMemberId(memberId);
    	newDelegationMember.setDelegationId(delegation.getDelegationId());
    	newDelegationMember.setRoleMemberId(roleMemberId);
    	newDelegationMember.setMemberTypeCode(memberTypeCode);
    	newDelegationMember.setActiveFromDate(activeFromDate);
    	newDelegationMember.setActiveToDate(activeToDate);

    	// build role member attribute objects from the given AttributeSet
    	addDelegationMemberAttributeData( newDelegationMember, qualifications, role.getKimTypeId() );

    	List<KimDelegationMemberImpl> delegationMembers = new ArrayList<KimDelegationMemberImpl>();
    	delegationMembers.add(newDelegationMember);
    	delegation.setMembers(delegationMembers);

    	getBusinessObjectService().save(delegation);
    	for(KimDelegationMemberImpl delegationMember: delegation.getMembers()){
    		deleteNullDelegationMemberAttributeData(delegationMember.getAttributes());
    	}
    	getIdentityManagementNotificationService().roleUpdated();
    }

    public RoleMemberCompleteInfo saveRoleMemberForRole(String roleMemberId, String memberId, String memberTypeCode, String roleId, 
    		AttributeSet qualifications, Date activeFromDate, Date activeToDate) throws UnsupportedOperationException{
    	if(StringUtils.isEmpty(roleMemberId) && StringUtils.isEmpty(memberId) && StringUtils.isEmpty(roleId)){
    		throw new IllegalArgumentException("Either Role member ID or a combination of member ID and role ID must be passed in.");
    	}
    	RoleMemberImpl origRoleMember = null;
    	RoleImpl role;
    	// create the new role member object
    	RoleMemberImpl newRoleMember = new RoleMemberImpl();
    	if(StringUtils.isEmpty(roleMemberId)){
	    	// look up the role
	    	role = getRoleImpl(roleId);
	    	// check that identical member does not already exist
	    	origRoleMember = matchingMemberRecord( role.getMembers(), memberId, memberTypeCode, qualifications );
    	} else{
    		origRoleMember = getRoleMemberImpl(roleMemberId);
    		role = getRoleImpl(origRoleMember.getRoleId());
    	}
    	
    	if(origRoleMember !=null){
    		newRoleMember.setRoleMemberId(origRoleMember.getRoleMemberId());
    		newRoleMember.setVersionNumber(origRoleMember.getVersionNumber());
    	} else {
	    	// get a new ID from the sequence
	    	SequenceAccessorService sas = getSequenceAccessorService();
	    	Long nextSeq = sas.getNextAvailableSequenceNumber(
	    			KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S, RoleMemberImpl.class);
	    	newRoleMember.setRoleMemberId( nextSeq.toString() );
    	}
    	newRoleMember.setRoleId(role.getRoleId());
    	newRoleMember.setMemberId( memberId );
    	newRoleMember.setMemberTypeCode( memberTypeCode );
    	newRoleMember.setActiveFromDate(activeFromDate);
    	newRoleMember.setActiveToDate(activeToDate);
    	// build role member attribute objects from the given AttributeSet
    	addMemberAttributeData( newRoleMember, qualifications, role.getKimTypeId() );

    	// When members are added to roles, clients must be notified.
    	getResponsibilityInternalService().saveRoleMember(newRoleMember);
    	deleteNullMemberAttributeData(newRoleMember.getAttributes());    
    	getIdentityManagementNotificationService().roleUpdated();
    	
    	return findRoleMemberCompleteInfo(newRoleMember.getRoleMemberId());
    }

    public void saveRoleRspActions(String roleResponsibilityActionId, String roleId, String roleResponsibilityId, String roleMemberId, 
			String actionTypeCode, String actionPolicyCode, Integer priorityNumber, Boolean forceAction){
		RoleResponsibilityActionImpl newRoleRspAction = new RoleResponsibilityActionImpl();
		newRoleRspAction.setActionPolicyCode(actionPolicyCode);
		newRoleRspAction.setActionTypeCode(actionTypeCode);
		newRoleRspAction.setPriorityNumber(priorityNumber);
		newRoleRspAction.setForceAction(forceAction);
		newRoleRspAction.setRoleMemberId(roleMemberId);
		newRoleRspAction.setRoleResponsibilityId(roleResponsibilityId);
		if(StringUtils.isEmpty(roleResponsibilityActionId)){
			//If there is an existing one
			Map<String, String> criteria = new HashMap<String, String>(1);		
			criteria.put(KimConstants.PrimaryKeyConstants.ROLE_RESPONSIBILITY_ID, roleResponsibilityId);
			criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMemberId);
			List<RoleResponsibilityActionImpl> roleResponsibilityActionImpls = (List<RoleResponsibilityActionImpl>)
				getBusinessObjectService().findMatching(RoleResponsibilityActionImpl.class, criteria);
			if(roleResponsibilityActionImpls!=null && roleResponsibilityActionImpls.size()>0){
				newRoleRspAction.setRoleResponsibilityActionId(roleResponsibilityActionImpls.get(0).getRoleResponsibilityActionId());
				newRoleRspAction.setVersionNumber(roleResponsibilityActionImpls.get(0).getVersionNumber());
			} else{
	//			 get a new ID from the sequence
		    	SequenceAccessorService sas = getSequenceAccessorService();
		    	Long nextSeq = sas.getNextAvailableSequenceNumber(
		    			KimConstants.SequenceNames.KRIM_ROLE_RSP_ACTN_ID_S, RoleResponsibilityActionImpl.class);
		    	newRoleRspAction.setRoleResponsibilityActionId(nextSeq.toString());
			}
		} else{
			Map<String, String> criteria = new HashMap<String, String>(1);		
			criteria.put(KimConstants.PrimaryKeyConstants.ROLE_RESPONSIBILITY_ACTION_ID, roleResponsibilityActionId);
			List<RoleResponsibilityActionImpl> roleResponsibilityActionImpls = (List<RoleResponsibilityActionImpl>)
				getBusinessObjectService().findMatching(RoleResponsibilityActionImpl.class, criteria);
			if(CollectionUtils.isNotEmpty(roleResponsibilityActionImpls) && roleResponsibilityActionImpls.size()>0){
				newRoleRspAction.setRoleResponsibilityActionId(roleResponsibilityActionImpls.get(0).getRoleResponsibilityActionId());
				newRoleRspAction.setVersionNumber(roleResponsibilityActionImpls.get(0).getVersionNumber());
			}
		}
		getBusinessObjectService().save(newRoleRspAction);
		getIdentityManagementNotificationService().roleUpdated();
	}
    
    // --------------------
    // Persistence Methods
    // --------------------

	// TODO: pulling attribute IDs repeatedly is inefficient - consider caching the entire list as a map

	@SuppressWarnings("unchecked")
	protected String getKimAttributeId( String attributeName ) {
		Map<String,Object> critieria = new HashMap<String,Object>( 1 );
		critieria.put( "attributeName", attributeName );
		Collection<KimAttributeImpl> defs = getBusinessObjectService().findMatching( KimAttributeImpl.class, critieria );
		return defs.iterator().next().getKimAttributeId();
	}
	
	private void deleteNullMemberAttributeData(List<RoleMemberAttributeDataImpl> attributes) {
		List<RoleMemberAttributeDataImpl> attributesToDelete = new ArrayList<RoleMemberAttributeDataImpl>();
		for(RoleMemberAttributeDataImpl attribute: attributes){
			if(attribute.getAttributeValue()==null){
				attributesToDelete.add(attribute);
			}
		}
		getBusinessObjectService().delete(attributesToDelete);
	}
	
	private void deleteNullDelegationMemberAttributeData(List<KimDelegationMemberAttributeDataImpl> attributes) {
		List<KimDelegationMemberAttributeDataImpl> attributesToDelete = new ArrayList<KimDelegationMemberAttributeDataImpl>();
		for(KimDelegationMemberAttributeDataImpl attribute: attributes){
			if(attribute.getAttributeValue()==null){
				attributesToDelete.add(attribute);
			}
		}
		getBusinessObjectService().delete(attributesToDelete);
	}
	
    protected void addMemberAttributeData( RoleMemberImpl roleMember, AttributeSet qualifier, String kimTypeId ) {
		List<RoleMemberAttributeDataImpl> attributes = new ArrayList<RoleMemberAttributeDataImpl>();
		for ( String attributeName : qualifier.keySet() ) {
			RoleMemberAttributeDataImpl a = new RoleMemberAttributeDataImpl();
			a.setAttributeValue( qualifier.get( attributeName ) );
			a.setKimTypeId( kimTypeId );
			a.setRoleMemberId( roleMember.getRoleMemberId() );
			// look up the attribute ID
			a.setKimAttributeId( getKimAttributeId( attributeName ) );
			
	    	Map<String, String> criteria = new HashMap<String, String>();
	    	criteria.put(KimConstants.PrimaryKeyConstants.KIM_ATTRIBUTE_ID, a.getKimAttributeId());
	    	criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMember.getRoleMemberId());
			List<RoleMemberAttributeDataImpl> origRoleMemberAttributes = 
	    		(List<RoleMemberAttributeDataImpl>)getBusinessObjectService().findMatching(RoleMemberAttributeDataImpl.class, criteria);
			RoleMemberAttributeDataImpl origRoleMemberAttribute = 
	    		(origRoleMemberAttributes!=null && origRoleMemberAttributes.size()>0) ? origRoleMemberAttributes.get(0) : null;
	    	if(origRoleMemberAttribute!=null){
	    		a.setAttributeDataId(origRoleMemberAttribute.getAttributeDataId());
	    		a.setVersionNumber(origRoleMemberAttribute.getVersionNumber());
	    	} else{
				// pull the next sequence number for the data ID
				a.setAttributeDataId(getNewAttributeDataId());
	    	}
			attributes.add( a );
		}
		roleMember.setAttributes( attributes );
	}
    
    protected void addDelegationMemberAttributeData( KimDelegationMemberImpl delegationMember, AttributeSet qualifier, String kimTypeId ) {
		List<KimDelegationMemberAttributeDataImpl> attributes = new ArrayList<KimDelegationMemberAttributeDataImpl>();
		for ( String attributeName : qualifier.keySet() ) {
			KimDelegationMemberAttributeDataImpl a = new KimDelegationMemberAttributeDataImpl();
			a.setAttributeValue( qualifier.get( attributeName ) );
			a.setKimTypeId( kimTypeId );
			a.setDelegationMemberId( delegationMember.getDelegationMemberId() );
			// look up the attribute ID
			a.setKimAttributeId( getKimAttributeId( attributeName ) );
	    	Map<String, String> criteria = new HashMap<String, String>();
	    	criteria.put(KimConstants.PrimaryKeyConstants.KIM_ATTRIBUTE_ID, a.getKimAttributeId());
	    	criteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMember.getDelegationMemberId());
			List<KimDelegationMemberAttributeDataImpl> origDelegationMemberAttributes = 
	    		(List<KimDelegationMemberAttributeDataImpl>)getBusinessObjectService().findMatching(KimDelegationMemberAttributeDataImpl.class, criteria);
			KimDelegationMemberAttributeDataImpl origDelegationMemberAttribute = 
	    		(origDelegationMemberAttributes!=null && origDelegationMemberAttributes.size()>0) ? origDelegationMemberAttributes.get(0) : null;
	    	if(origDelegationMemberAttribute!=null){
	    		a.setAttributeDataId(origDelegationMemberAttribute.getAttributeDataId());
	    		a.setVersionNumber(origDelegationMemberAttribute.getVersionNumber());
	    	} else{
				// pull the next sequence number for the data ID
				a.setAttributeDataId(getNewAttributeDataId());
	    	}
			attributes.add( a );
		}
		delegationMember.setAttributes( attributes );
	}

}
