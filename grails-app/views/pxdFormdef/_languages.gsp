<%-- == Motrice Copyright Notice ==

  Motrice BPM

  Copyright (C) 2011-2015 Motrice AB

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.

  e-mail: info _at_ motrice.se
  mail: Motrice AB, Halmstadsvägen 16, SE-121 51 JOHANNESHOV, SWEDEN
  phone: +46 73 341 4983

--%>
<%@ page import="org.motrice.postxdb.PxdFormdef" %>
<ol class="property-list pxdFormdef">
  <g:if test="${pxdFormdefObj?.path}">
    <li class="fieldcontain">
      <span id="path-label" class="property-label"><g:message code="pxdFormdef.path.label" default="Path" /></span>
      <span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${pxdFormdefObj}" field="path"/></span>
    </li>
  </g:if>
  <g:if test="${pxdFormdefObj?.currentDraft}">
    <li class="fieldcontain">
      <span id="currentDraft-label" class="property-label"><g:message code="pxdFormdef.currentDraft.label" default="Current Draft" /></span>
      <span class="property-value" aria-labelledby="currentDraft-label"><g:fieldValue bean="${pxdFormdefObj}" field="currentDraft"/></span>
    </li>
  </g:if>
  <g:if test="${pxdFormdefObj?.uuid}">
    <li class="fieldcontain">
      <span id="uuid-label" class="property-label"><g:message code="pxdFormdef.uuid.label" default="Uuid" /></span>
      <span class="property-value" aria-labelledby="uuid-label"><g:fieldValue bean="${pxdFormdefObj}" field="uuid"/></span>
    </li>
  </g:if>
  <g:if test="${pxdFormdefObj?.dateCreated}">
    <li class="fieldcontain">
      <span id="dateCreated-label" class="property-label"><g:message code="pxdFormdef.createdUpdated.label" default="Date Created" /></span>
      <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${pxdFormdefObj?.dateCreated}"/>/
	<g:formatDate date="${pxdFormdefObj?.lastUpdated}"/></span>
    </li>
  </g:if>
</ol>
<div class="fieldcontain ${hasErrors(bean: pxdFormdefObj, field: 'srcLang', 'error')} ">
  <label for="srcLang">
    <g:message code="pxdFormdef.srcLang.label" default="SrcLang" />
  </label>
  <g:textField name="srcLang" maxlength="8" value=""/>
</div>
<div class="fieldcontain ${hasErrors(bean: pxdFormdefObj, field: 'tgtLang', 'error')} ">
  <label for="tgtLang">
    <g:message code="pxdFormdef.tgtLang.label" default="TgtLang" />
  </label>
  <g:textField name="tgtLang" maxlength="8" value=""/>
</div>
<div class="fieldcontain"/>

<p><g:message code="pxdFormdef.addLanguage.help"/></p>
