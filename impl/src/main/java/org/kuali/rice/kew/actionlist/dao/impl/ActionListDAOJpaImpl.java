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
package org.kuali.rice.kew.actionlist.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.jpa.criteria.Criteria;
import org.kuali.rice.core.jpa.criteria.QueryByCriteria;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.OutboxItemActionListExtension;
import org.kuali.rice.kew.actionlist.ActionListFilter;
import org.kuali.rice.kew.actionlist.dao.ActionListDAO;
import org.kuali.rice.kew.user.WorkflowUser;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.service.KIMServiceLocator;


/**
 * OJB implementation of the {@link ActionListDAO}.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class ActionListDAOJpaImpl implements ActionListDAO {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ActionListDAOJpaImpl.class);

    @PersistenceContext(unitName="kew-unit")
	private EntityManager entityManager;

    public Collection<ActionItem> getActionList(String principalId, ActionListFilter filter) {
        return getActionItemsInActionList(ActionItem.class, principalId, filter);

//        LOG.debug("getting action list for user " + workflowUser.getWorkflowUserId().getWorkflowId());
//        Criteria crit = null;
//        if (filter == null) {
//            crit = new Criteria();
//            crit.addEqualTo("workflowId", workflowUser.getWorkflowUserId().getWorkflowId());
//        } else {
//            crit = setUpActionListCriteria(workflowUser, filter);
//        }
//        LOG.debug("running query to get action list for criteria " + crit);
//        Collection<ActionItem> collection = this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ActionItemActionListExtension.class, crit));
//        LOG.debug("found " + collection.size() + " action items for user " + workflowUser.getWorkflowUserId().getWorkflowId());
//        return createActionListForUser(collection);
    }

    public Collection<ActionItem> getActionListForSingleDocument(Long routeHeaderId) {
        LOG.debug("getting action list for route header id " + routeHeaderId);
        Criteria crit = new Criteria(ActionItem.class.getName());
        crit.eq("routeHeaderId", routeHeaderId);
        Collection<ActionItem> collection = new QueryByCriteria(entityManager, crit).toQuery().getResultList();
        LOG.debug("found " + collection.size() + " action items for route header id " + routeHeaderId);
        return createActionListForRouteHeader(collection);
    }

    private Criteria setUpActionListCriteria(Class objectsToRetrieve, String principalId, ActionListFilter filter) {
        LOG.debug("setting up Action List criteria");
        Criteria crit = new Criteria(objectsToRetrieve.getName());
        boolean filterOn = false;
        String filteredByItems = "";

        if (filter.getActionRequestCd() != null && !"".equals(filter.getActionRequestCd().trim()) && !filter.getActionRequestCd().equals(KEWConstants.ALL_CODE)) {
            if (filter.isExcludeActionRequestCd()) {
                crit.ne("actionRequestCd", filter.getActionRequestCd());
            } else {
                crit.eq("actionRequestCd", filter.getActionRequestCd());
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Action Requested";
        }

        if (filter.getCreateDateFrom() != null || filter.getCreateDateTo() != null) {
            if (filter.isExcludeCreateDate()) {
                if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() != null) {
                    crit.notBetween("routeHeader.createDate", new Timestamp(beginningOfDay(filter.getCreateDateFrom()).getTime()), new Timestamp(endOfDay(filter.getCreateDateTo()).getTime()));
                } else if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() == null) {
                    crit.lte("routeHeader.createDate", new Timestamp(beginningOfDay(filter.getCreateDateFrom()).getTime()));
                } else if (filter.getCreateDateFrom() == null && filter.getCreateDateTo() != null) {
                    crit.gte("routeHeader.createDate", new Timestamp(endOfDay(filter.getCreateDateTo()).getTime()));
                }
            } else {
                if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() != null) {
                    crit.between("routeHeader.createDate", new Timestamp(beginningOfDay(filter.getCreateDateFrom()).getTime()), new Timestamp(endOfDay(filter.getCreateDateTo()).getTime()));
                } else if (filter.getCreateDateFrom() != null && filter.getCreateDateTo() == null) {
                    crit.gte("routeHeader.createDate", new Timestamp(beginningOfDay(filter.getCreateDateFrom()).getTime()));
                } else if (filter.getCreateDateFrom() == null && filter.getCreateDateTo() != null) {
                    crit.lte("routeHeader.createDate", new Timestamp(endOfDay(filter.getCreateDateTo()).getTime()));
                }
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Date Created";
        }

        if (filter.getDocRouteStatus() != null && !"".equals(filter.getDocRouteStatus().trim()) && !filter.getDocRouteStatus().equals(KEWConstants.ALL_CODE)) {
            if (filter.isExcludeRouteStatus()) {
                crit.ne("routeHeader.docRouteStatus", filter.getDocRouteStatus());
            } else {
                crit.eq("routeHeader.docRouteStatus", filter.getDocRouteStatus());
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Document Route Status";
        }

        if (filter.getDocumentTitle() != null && !"".equals(filter.getDocumentTitle().trim())) {
            String docTitle = filter.getDocumentTitle();
            if (docTitle.trim().endsWith("*")) {
                docTitle = docTitle.substring(0, docTitle.length() - 1);
            }

            if (filter.isExcludeDocumentTitle()) {
                crit.notLike("docTitle", "%" + docTitle + "%");
            } else {
                crit.like("docTitle", "%" + docTitle + "%");
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Document Title";
        }

        if (filter.getDocumentType() != null && !"".equals(filter.getDocumentType().trim())) {
            if (filter.isExcludeDocumentType()) {
                crit.notLike("docName", "%" + filter.getDocumentType() + "%");
            } else {
                crit.like("docName", "%" + filter.getDocumentType() + "%");
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Document Type";
        }

        if (filter.getLastAssignedDateFrom() != null || filter.getLastAssignedDateTo() != null) {
            if (filter.isExcludeLastAssignedDate()) {
                if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() != null) {
                    crit.notBetween("dateAssigned", new Timestamp(beginningOfDay(filter.getLastAssignedDateFrom()).getTime()), new Timestamp(endOfDay(filter.getLastAssignedDateTo()).getTime()));
                } else if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() == null) {
                    crit.lte("dateAssigned", new Timestamp(beginningOfDay(filter.getLastAssignedDateFrom()).getTime()));
                } else if (filter.getLastAssignedDateFrom() == null && filter.getLastAssignedDateTo() != null) {
                    crit.gte("dateAssigned", new Timestamp(endOfDay(filter.getLastAssignedDateTo()).getTime()));
                }
            } else {
                if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() != null) {
                    crit.between("dateAssigned", new Timestamp(beginningOfDay(filter.getLastAssignedDateFrom()).getTime()), new Timestamp(endOfDay(filter.getLastAssignedDateTo()).getTime()));
                } else if (filter.getLastAssignedDateFrom() != null && filter.getLastAssignedDateTo() == null) {
                    crit.gte("dateAssigned", new Timestamp(beginningOfDay(filter.getLastAssignedDateFrom()).getTime()));
                } else if (filter.getLastAssignedDateFrom() == null && filter.getLastAssignedDateTo() != null) {
                    crit.lte("dateAssigned", new Timestamp(endOfDay(filter.getLastAssignedDateTo()).getTime()));
                }
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Date Last Assigned";
        }

        filter.setGroupId(null);
        if (filter.getGroupIdString() != null && !"".equals(filter.getGroupIdString().trim()) && !filter.getGroupIdString().trim().equals(KEWConstants.NO_FILTERING)) {
            filter.setGroupId(filter.getGroupId());
            if (filter.isExcludeGroupId()) {
                Criteria critNotEqual = new Criteria(objectsToRetrieve.getName());
                critNotEqual.ne("groupId", filter.getGroupId());
                Criteria critNull = new Criteria(objectsToRetrieve.getName());
                critNull.isNull("groupId");
                critNotEqual.or(critNull);
                crit.and(critNotEqual);
            } else {
                crit.eq("groupId", filter.getGroupId());
            }
            filteredByItems += filteredByItems.length() > 0 ? ", " : "";
            filteredByItems += "Action Request Workgroup";
        }

        if (filteredByItems.length() > 0) {
            filterOn = true;
        }

        boolean addedDelegationCriteria = false;
        if (StringUtils.isBlank(filter.getDelegationType()) && StringUtils.isBlank(filter.getPrimaryDelegateId()) && StringUtils.isBlank(filter.getDelegatorId())) {
            crit.eq("principalId", principalId);
            addedDelegationCriteria = true;
        } else if ((StringUtils.isNotBlank(filter.getDelegationType()) && KEWConstants.DELEGATION_PRIMARY.equals(filter.getDelegationType()))
                || StringUtils.isNotBlank(filter.getPrimaryDelegateId())) {
            // using a primary delegation
            if ((StringUtils.isBlank(filter.getPrimaryDelegateId())) || (filter.getPrimaryDelegateId().trim().equals(KEWConstants.ALL_CODE))) {
                // user wishes to see all primary delegations
                Criteria userCrit = new Criteria(objectsToRetrieve.getName());
                Criteria groupCrit = new Criteria(objectsToRetrieve.getName());
                Criteria orCrit = new Criteria(objectsToRetrieve.getName());
                userCrit.eq("delegatorWorkflowId", principalId);
                groupCrit.in("delegatorGroupId", new ArrayList(KIMServiceLocator.getIdentityManagementService().getGroupIdsForPrincipal(principalId)));
                orCrit.or(userCrit);
                orCrit.or(groupCrit);
                crit.and(orCrit);
                crit.eq("delegationType", KEWConstants.DELEGATION_PRIMARY);
                filter.setDelegationType(KEWConstants.DELEGATION_PRIMARY);
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Primary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            } else if (!filter.getPrimaryDelegateId().trim().equals(KEWConstants.PRIMARY_DELEGATION_DEFAULT)) {
                // user wishes to see primary delegation for a single user
                crit.eq("principalId", filter.getPrimaryDelegateId());
                Criteria userCrit = new Criteria(objectsToRetrieve.getName());
                Criteria groupCrit = new Criteria(objectsToRetrieve.getName());
                Criteria orCrit = new Criteria(objectsToRetrieve.getName());
                userCrit.eq("delegatorWorkflowId", principalId);
                groupCrit.in("delegatorGroupId", new ArrayList(KIMServiceLocator.getIdentityManagementService().getGroupIdsForPrincipal(principalId)));
                orCrit.or(userCrit);
                orCrit.or(groupCrit);
                crit.and(orCrit);
                crit.eq("delegationType", KEWConstants.DELEGATION_PRIMARY);
                filter.setDelegationType(KEWConstants.DELEGATION_PRIMARY);
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Primary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            }
        } else if ((StringUtils.isNotBlank(filter.getDelegationType()) && KEWConstants.DELEGATION_SECONDARY.equals(filter.getDelegationType()))
                || StringUtils.isNotBlank(filter.getDelegatorId())) {
            // using a secondary delegation
            crit.eq("principalId", principalId);
            if (StringUtils.isBlank(filter.getDelegatorId())) {
                filter.setDelegationType(KEWConstants.DELEGATION_SECONDARY);
                // if isExcludeDelegationType() we want to show the default aciton list which is set up later in this method
                if (!filter.isExcludeDelegationType()) {
                    crit.eq("delegationType", KEWConstants.DELEGATION_SECONDARY);
                    addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                    addedDelegationCriteria = true;
                    filterOn = true;
                }
            } else if (filter.getDelegatorId().trim().equals(KEWConstants.ALL_CODE)) {
                // user wishes to see all secondary delegations
                crit.eq("delegationType", KEWConstants.DELEGATION_SECONDARY);
                filter.setDelegationType(KEWConstants.DELEGATION_SECONDARY);
                filter.setExcludeDelegationType(false);
                addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            } else if (!filter.getDelegatorId().trim().equals(
                    KEWConstants.DELEGATION_DEFAULT)) {
                // user has specified an id to see for secondary delegation
                filter.setDelegationType(KEWConstants.DELEGATION_SECONDARY);
                filter.setExcludeDelegationType(false);
                Criteria userCrit = new Criteria(objectsToRetrieve.getName());
                Criteria groupCrit = new Criteria(objectsToRetrieve.getName());
                if (filter.isExcludeDelegatorId()) {
                    Criteria userNull = new Criteria(objectsToRetrieve.getName());
                    userCrit.ne("delegatorWorkflowId", filter.getDelegatorId());
                    userNull.isNull("delegatorWorkflowId");
                    userCrit.or(userNull);
                    Criteria groupNull = new Criteria(objectsToRetrieve.getName());
                    groupCrit.ne("delegatorGroupId", Long.valueOf(filter.getDelegatorId()));
                    groupNull.isNull("delegatorGroupId");
                    groupCrit.or(groupNull);
                    crit.and(userCrit);
                    crit.and(groupCrit);
                } else {
                    userCrit.eq("delegatorWorkflowId", filter.getDelegatorId());
                    groupCrit.eq("delegatorGroupId", Long.valueOf(filter.getDelegatorId()));
                    userCrit.or(groupCrit);
                    crit.and(userCrit);
                }
                addToFilterDescription(filteredByItems, "Secondary Delegator Id");
                addedDelegationCriteria = true;
                filterOn = true;
            }
        }

        // if we haven't added delegation criteria then use the default criteria below
        if (!addedDelegationCriteria) {
            crit.eq("principalId", principalId);
            filter.setDelegationType(KEWConstants.DELEGATION_SECONDARY);
            filter.setExcludeDelegationType(true);
            Criteria critNotEqual = new Criteria(objectsToRetrieve.getName());
            Criteria critNull = new Criteria(objectsToRetrieve.getName());
            critNotEqual.ne("delegationType", KEWConstants.DELEGATION_SECONDARY);
            critNull.isNull("delegationType");
            critNotEqual.or(critNull);
            crit.and(critNotEqual);
        }


        if (! "".equals(filteredByItems)) {
            filteredByItems = "Filtered by " + filteredByItems;
        }
        filter.setFilterLegend(filteredByItems);
        filter.setFilterOn(filterOn);

        LOG.debug("returning from Action List criteria");
        return crit;
    }

    private void addToFilterDescription(String filterDescription, String labelToAdd) {
        filterDescription += filterDescription.length() > 0 ? ", " : "";
        filterDescription += labelToAdd;
    }

    private static final String ACTION_LIST_COUNT_QUERY = "select count(distinct(ai.doc_hdr_id)) from krew_actn_itm_t ai where ai.PRNCPL_ID = ? and (ai.dlgn_typ is null or ai.dlgn_typ = 'P')";

    public int getCount(final String workflowId) {

    	javax.persistence.Query q = entityManager.createNativeQuery(ACTION_LIST_COUNT_QUERY);
    	q.setParameter(1, workflowId);
    	Long result = (Long)q.getSingleResult();
    	return result.intValue();
    }

    /**
     * Creates an Action List from the given collection of Action Items.  The Action List should
     * contain only one action item per document.  The action item chosen should be the most "critical"
     * or "important" one on the document.
     *
     * @return the Action List as a Collection of ActionItems
     */
    private Collection<ActionItem> createActionListForUser(Collection<ActionItem> actionItems) {
        Map<Long, ActionItem> actionItemMap = new HashMap<Long, ActionItem>();
        ActionListPriorityComparator comparator = new ActionListPriorityComparator();
        for (ActionItem potentialActionItem: actionItems) {
            ActionItem existingActionItem = actionItemMap.get(potentialActionItem.getRouteHeaderId());
            if (existingActionItem == null || comparator.compare(potentialActionItem, existingActionItem) > 0) {
                actionItemMap.put(potentialActionItem.getRouteHeaderId(), potentialActionItem);
            }
        }
        return actionItemMap.values();
    }

    /**
     * Creates an Action List from the given collection of Action Items.  The Action List should
     * contain only one action item per user.  The action item chosen should be the most "critical"
     * or "important" one on the document.
     *
     * @return the Action List as a Collection of ActionItems
     */
    private Collection<ActionItem> createActionListForRouteHeader(Collection<ActionItem> actionItems) {
        Map<String, ActionItem> actionItemMap = new HashMap<String, ActionItem>();
        ActionListPriorityComparator comparator = new ActionListPriorityComparator();
        for (ActionItem potentialActionItem: actionItems) {
            ActionItem existingActionItem = actionItemMap.get(potentialActionItem.getPrincipalId());
            if (existingActionItem == null || comparator.compare(potentialActionItem, existingActionItem) > 0) {
                actionItemMap.put(potentialActionItem.getPrincipalId(), potentialActionItem);
            }
        }
        return actionItemMap.values();
    }

    private Collection<ActionItem> getActionItemsInActionList(Class objectsToRetrieve, String principalId, ActionListFilter filter) {
        LOG.debug("getting action list for user " + principalId);
        Criteria crit = null;
        if (filter == null) {
            crit = new Criteria(objectsToRetrieve.getName());
            crit.eq("principalId", principalId);
        } else {
            crit = setUpActionListCriteria(objectsToRetrieve, principalId, filter);
        }
        LOG.debug("running query to get action list for criteria " + crit);
        Collection<ActionItem> collection = new QueryByCriteria(entityManager, crit).toQuery().getResultList();
        LOG.debug("found " + collection.size() + " action items for user " + principalId);
        return createActionListForUser(collection);
    }

    public Collection<ActionItem> getOutbox(String principalId, ActionListFilter filter) {
        return getActionItemsInActionList(OutboxItemActionListExtension.class, principalId, filter);
//        LOG.debug("getting action list for user " + workflowUser.getWorkflowUserId().getWorkflowId());
//        Criteria crit = new Criteria();
//        crit.addEqualTo("workflowId", workflowUser.getWorkflowUserId().getWorkflowId());
//        if (filter != null) {
//            setUpActionListCriteria(workflowUser, filter);
//        }
//        LOG.debug("running query to get action list for criteria " + crit);
//        Collection<ActionItem> collection = this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(OutboxItemActionListExtension.class, crit));
//        LOG.debug("found " + collection.size() + " action items for user " + workflowUser.getWorkflowUserId().getWorkflowId());
//        return createActionListForUser(collection);
    }

    /**
     *
     * Deletes all outbox items specified by the list of ids
     *
     * @see org.kuali.rice.kew.actionlist.dao.ActionListDAO#removeOutboxItems(java.lang.String, java.util.List)
     */
    public void removeOutboxItems(String principalId, List<Long> outboxItems) {
        Criteria crit = new Criteria(OutboxItemActionListExtension.class.getName());
        crit.in("actionItemId", outboxItems);
        for(Object entity:new QueryByCriteria(entityManager, crit).toQuery().getResultList()){
        	entityManager.remove(entity);
        }
    }

    /**
     * Saves an outbox item
     *
     * @see org.kuali.rice.kew.actionlist.dao.ActionListDAO#saveOutboxItem(org.kuali.rice.kew.actionitem.OutboxItemActionListExtension)
     */
    public void saveOutboxItem(OutboxItemActionListExtension outboxItem) {
    	entityManager.merge(outboxItem); //TODO, merge will not update the outboxitem pointer to the merged entity
    }

    /**
     * Gets the outbox item associated with the document id
     *
     * @see org.kuali.rice.kew.actionlist.dao.ActionListDAO#getOutboxByDocumentId(java.lang.Long)
     */
    public OutboxItemActionListExtension getOutboxByDocumentId(Long documentId) {
        Criteria crit = new Criteria(OutboxItemActionListExtension.class.getName());
        crit.eq("routeHeaderId", documentId);
        return (OutboxItemActionListExtension) new QueryByCriteria(entityManager, crit).toQuery().getSingleResult();
    }

    /**
     * This overridden method ...
     *
     * @see org.kuali.rice.kew.actionlist.dao.ActionListDAO#getOutboxByDocumentIdUserId(java.lang.Long)
     */
    public OutboxItemActionListExtension getOutboxByDocumentIdUserId(Long documentId, String userId) {
        Criteria crit = new Criteria(OutboxItemActionListExtension.class.getName());
        crit.eq("routeHeaderId", documentId);
        crit.eq("principalId", userId);
        return (OutboxItemActionListExtension) new QueryByCriteria(entityManager, crit).toQuery().getSingleResult();
    }

    private Date beginningOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

}