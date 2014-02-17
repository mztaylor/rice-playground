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
package org.kuali.rice.krad.uif.lifecycle.initialize;

import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.lifecycle.LifecycleElementState;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleTaskBase;

/**
 * Sort items in a container
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SortContainerTask extends ViewLifecycleTaskBase<Container> {

    /**
     * Creates an instance based on element state.
     * 
     * @param elementState lifecycle element state information
     */
    protected SortContainerTask(LifecycleElementState elementState) {
        super(elementState, Container.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performLifecycleTask() {
            Container container = (Container) getElementState().getElement();
            container.sortItems();
    }

}