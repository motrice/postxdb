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
