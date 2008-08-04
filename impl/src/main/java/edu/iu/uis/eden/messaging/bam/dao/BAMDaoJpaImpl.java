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
package edu.iu.uis.eden.messaging.bam.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.namespace.QName;

import org.kuali.rice.core.reflect.ObjectDefinition;

import edu.iu.uis.eden.messaging.bam.BAMTargetEntry;

public class BAMDaoJpaImpl implements BAMDAO {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void clearBAMTables() {
        entityManager.createQuery("delete bte from BAMTargetEntry bte").executeUpdate();
        entityManager.createQuery("delete bp from BAMParam bp").executeUpdate();        
    }

    public List<BAMTargetEntry> getCallsForService(QName serviceName, String methodName) {       
        return (List<BAMTargetEntry>) entityManager.createQuery("select bte from BAMTargetEntry bte where bte.serviceName = :serviceName and bte.methodName = :methodName").setParameter("serviceName", serviceName.toString()).setParameter("methodName", methodName).getResultList();
    }

    public void save(BAMTargetEntry bamEntry) {
        entityManager.persist(bamEntry);
    }

    public List<BAMTargetEntry> getCallsForService(QName serviceName) {
        return (List<BAMTargetEntry>) entityManager.createQuery("select bte from BAMTargetEntry bte where bte.serviceName = :serviceName").setParameter("serviceName", serviceName.toString()).getResultList();
    }

    public List<BAMTargetEntry> getCallsForRemotedClasses(ObjectDefinition objDef) {
        QName qname = new QName(objDef.getMessageEntity(), objDef.getClassName());
        return (List<BAMTargetEntry>) entityManager.createQuery("select bte from BAMTargetEntry bte where bte.serviceName like :serviceName%").setParameter("serviceName", qname.toString()).getResultList();
    }

    public List<BAMTargetEntry> getCallsForRemotedClasses(ObjectDefinition objDef, String methodName) {
        QName qname = new QName(objDef.getMessageEntity(), objDef.getClassName());
        return (List<BAMTargetEntry>) entityManager.createQuery("select bte from BAMTargetEntry bte where bte.serviceName like :serviceName% and bte.methodName = :methodName").setParameter("serviceName", qname.toString()).setParameter("methodName", methodName).getResultList();
    }

}
