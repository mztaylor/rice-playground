/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krms.impl.ui;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.rice.krad.maintenance.MaintainableImpl;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.form.MaintenanceDocumentForm;
import org.kuali.rice.krms.api.repository.context.ContextDefinition;
import org.kuali.rice.krms.impl.repository.ContextBo;
import org.kuali.rice.krms.impl.repository.ContextValidTermBo;
import org.kuali.rice.krms.impl.repository.KrmsRepositoryServiceLocator;
import org.kuali.rice.krms.impl.repository.TermSpecificationBo;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * {@link org.kuali.rice.krad.maintenance.Maintainable} for the {@link AgendaEditor}
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class TermSpecificationMaintainable extends MaintainableImpl {
	
	private static final long serialVersionUID = 1L;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TermSpecificationMaintainable.class);

    @Override
    public Object retrieveObjectForEditOrCopy(MaintenanceDocument document, Map<String, String> dataObjectKeys) {

        TermSpecificationBo termSpecificationBo = (TermSpecificationBo) super.retrieveObjectForEditOrCopy(document,
                dataObjectKeys);

        if (!CollectionUtils.isEmpty(termSpecificationBo.getContextValidTerms())) {
            for (ContextValidTermBo contextValidTerm : termSpecificationBo.getContextValidTerms()) {
                ContextDefinition context =
                        KrmsRepositoryServiceLocator.getContextBoService().getContextByContextId(contextValidTerm.getTermSpecificationId());

                if (context != null) {
                    termSpecificationBo.getContexts().add(ContextBo.from(context));
                    termSpecificationBo.getContextIds().add(contextValidTerm.getContextId());
                }
            }
        }

        if (KRADConstants.MAINTENANCE_COPY_ACTION.equals(getMaintenanceAction())) {
            document.getDocumentHeader().setDocumentDescription("New Term Specification Document");
        }

        return termSpecificationBo;
    }

    /**
	 * {@inheritDoc}
	 */
	@Override
	public void processAfterNew(MaintenanceDocument document,
		Map<String, String[]> requestParameters) {

		super.processAfterNew(document, requestParameters);
        document.getDocumentHeader().setDocumentDescription("New Term Specification Document");

	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        super.processAfterEdit(document, requestParameters);
        copyContextsOldToNewBo(document);
        document.getDocumentHeader().setDocumentDescription("Edited Term Specification Document");
    }

    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        super.processAfterCopy(document, requestParameters);
        copyContextsOldToNewBo(document);
    }

    /**
     * Copy the contexts from the old TermSpecificationBo to the newTermSpecificationBo of the maintenance document.
     * <p>
     * Since the contexts is a transient field it doesn't get copied by the deepCopy in
     * MaintenanceDocumentServiceImpl.setupMaintenanceObject, we manually need to copy the values over.
     * For performance reasons a shallow copy is done since the ContextBo themselves are never changed.
     * </p>
     * @param document that contains the old and new TermSpecificationBos
     */
    private void copyContextsOldToNewBo(MaintenanceDocument document) {
        TermSpecificationBo oldTermSpec = (TermSpecificationBo) document.getOldMaintainableObject().getDataObject();
        TermSpecificationBo newTermSpec = (TermSpecificationBo) document.getNewMaintainableObject().getDataObject();
        newTermSpec.setContexts(new ArrayList<ContextBo>());
        for (ContextBo contextBo : oldTermSpec.getContexts()) {
            newTermSpec.getContexts().add(contextBo);
        }
    }

    /**
     * Overrides the parent method to additionaly clear the contexts list, which is needed for serialization performance
     * & space.
     *
     * @see org.kuali.rice.krad.maintenance.Maintainable#prepareForSave
     */
    @Override
    public void prepareForSave() {
        super.prepareForSave();

        TermSpecificationBo termSpec = (TermSpecificationBo) getDataObject();
        termSpec.getContexts().clear();
    }

    /**
     * Adds the given context to the persisted contextValidTerms collection on the data object.  Without this step, the
     * context is only added to a transient collection and the relationship will never be persisted.
     */
    @Override
    public void processAfterAddLine(View view, CollectionGroup collectionGroup, Object model, Object addLine,
            boolean isValidLine) {
        super.processAfterAddLine(view, collectionGroup, model, addLine, isValidLine);

        ContextBo addedContext = (ContextBo) addLine;
        TermSpecificationBo termSpec = (TermSpecificationBo)((MaintenanceDocumentForm)model).getDocument().getNewMaintainableObject().getDataObject();

        boolean alreadyHasContextValidTerm = false;

        for (ContextValidTermBo contextValidTerm : termSpec.getContextValidTerms()) {
            if (contextValidTerm.getContextId().equals(addedContext.getId())) {
                alreadyHasContextValidTerm = true;
                break;
            }
        }

        if (!alreadyHasContextValidTerm) {
            ContextValidTermBo contextValidTerm = new ContextValidTermBo();
            contextValidTerm.setContextId(addedContext.getId());
            contextValidTerm.setTermSpecification(termSpec);
            termSpec.getContextValidTerms().add(contextValidTerm);
        }
    }

    /**
     * Removes the given context from the persisted contextValidTerms collection on the data object.  Without this step, the
     * context is only removed from a transient collection and the severed relationship will never be persisted.
     */
    @Override
    public void processCollectionDeleteLine(View view, Object model, String collectionPath, int lineIndex) {
        TermSpecificationBo termSpec = (TermSpecificationBo)((MaintenanceDocumentForm)model).getDocument().getNewMaintainableObject().getDataObject();
        List<ContextBo> collection = ObjectPropertyUtils.getPropertyValue(model, collectionPath);

        ContextBo context = collection.get(lineIndex);

        super.processCollectionDeleteLine(view, model, collectionPath, lineIndex);

        if (context == null) return;

        ListIterator<ContextValidTermBo> contextValidTermListIter = termSpec.getContextValidTerms().listIterator();
        while (contextValidTermListIter.hasNext()) {
            ContextValidTermBo contextValidTerm = contextValidTermListIter.next();

            if (contextValidTerm.getContextId().equals(context.getId())) {
                contextValidTerm.setTermSpecification(null);
                contextValidTermListIter.remove();
            }
        }
    }

    @Override
    public Class getDataObjectClass() {
        return TermSpecificationBo.class;
    }

    /**
     * Recreate the contexts from the contextIDs (needed due to serialization)
     *
     * @see org.kuali.rice.krad.maintenance.Maintainable#processAfterRetrieve
     */
    @Override
    public void processAfterRetrieve() {
        super.processAfterRetrieve();

        TermSpecificationBo termSpec = (TermSpecificationBo) getDataObject();
        termSpec.setContexts(new ArrayList<ContextBo>());
        for (ContextValidTermBo contextValidTerm : termSpec.getContextValidTerms()) {
            ContextDefinition context = KrmsRepositoryServiceLocator.getContextBoService().getContextByContextId(contextValidTerm.getContextId());
            termSpec.getContexts().add(ContextBo.from(context));
            termSpec.getContextIds().add(context.getId());
        }
    }
}