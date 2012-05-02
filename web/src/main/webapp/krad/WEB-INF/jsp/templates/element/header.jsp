<%--

    Copyright 2005-2012 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ include file="/krad/WEB-INF/jsp/tldHeader.jsp"%>

<tiles:useAttribute name="element" classname="org.kuali.rice.krad.uif.element.Header"/>

<c:if test="${!empty element.headerStyleClassesAsString}">
  <c:set var="styleClass" value="class=\"${element.headerStyleClassesAsString}\""/>
</c:if>

<c:if test="${!empty element.headerStyle}">
  <c:set var="style" value="style=\"${element.headerStyle}\""/>
</c:if>

<c:if test="${!empty element.headerLevel}">
  <c:set var="headerOpenTag" value="<${element.headerLevel} ${style} ${styleClass}>"/>
  <c:set var="headerCloseTag" value="</${element.headerLevel}>"/>
</c:if>

<krad:div component="${element}">
  <%-- upper group --%>
  <krad:template component="${element.upperGroup}"/>

  <c:if test="${!empty element.headerLevel && !empty element.headerText && element.headerText != '&nbsp;'}">
    <c:set var="group" value="${element.context['parent']}"/>
    ${headerOpenTag}<span class="uif-headerText-span">${element.headerText}</span>${headerCloseTag}
    <krad:template component="${group.help}"/>
  </c:if>

  <%-- right group --%>
  <krad:template component="${element.rightGroup}"/>

  <%-- lower group --%>
  <krad:template component="${element.lowerGroup}"/>
</krad:div>