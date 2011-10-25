package org.kuali.rice.core.impl.criteria

import org.junit.Test
import org.kuali.rice.core.api.criteria.QueryByCriteria
import org.kuali.rice.core.api.parameter.Parameter

import org.kuali.rice.krad.criteria.CriteriaLookupDaoOjb

class CriteriaLookupServiceOjbImplTest {

    def lookup = new CriteriaLookupDaoOjb();

    @Test(expected=IllegalArgumentException.class)
    void test_lookup_with_null_1() {
        lookup.lookup(null, QueryByCriteria.Builder.create().build())
    }

    @Test(expected=IllegalArgumentException.class)
    void test_lookup_with_null_2() {
        lookup.lookup(Parameter.class, null)
    }
}
