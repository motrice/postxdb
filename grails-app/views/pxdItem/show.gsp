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
%-- == Motrice Copyright Notice ==

  Motrice Service Platform

  Copyright (C) 2011-2014 Motrice AB

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
  mail: Motrice AB, Långsjövägen 8, SE-131 33 NACKA, SWEDEN
  phone: +46 8 641 64 14

--%>
<%@ page import="org.motrice.postxdb.PxdItem" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
      <g:set var="entityName" value="${message(code: 'pxdItem.label', default: 'PxdItem')}" />
      <title><g:message code="default.show.label" args="[entityName]" /></title>
    </meta>
  </head>
  <body>
    <a href="#show-pxdItem" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div id="show-pxdItem" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
      </g:if>
      <ol class="property-list pxdItem">
	<g:if test="${pxdItemObj?.path}">
	  <li class="fieldcontain">
	    <span id="path-label" class="property-label"><g:message code="pxdItem.path.label" default="Path" /></span>
	    <span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${pxdItemObj}" field="path"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.instance != null}">
	  <li class="fieldcontain">
	    <span id="instance-label" class="property-label"><g:message code="pxdItem.instance.label" default="Instance" /></span>
	    <span class="property-value" aria-labelledby="instance-label"><g:instflag flag="${pxdItemObj?.instance}"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.format}">
	  <li class="fieldcontain">
	    <span id="format-label" class="property-label"><g:message code="pxdItem.format.label" default="Format" /></span>
	    <span class="property-value" aria-labelledby="format-label"><g:fieldValue bean="${pxdItemObj}" field="format"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.size}">
	  <li class="fieldcontain">
	    <span id="size-label" class="property-label"><g:message code="pxdItem.size.label" default="Size" /></span>
	    <span class="property-value" aria-labelledby="size-label"><g:fieldValue bean="${pxdItemObj}" field="size"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.readOnly}">
	  <li class="fieldcontain">
	    <span id="readOnly-label" class="property-label"><g:message code="pxdItem.readOnly.label" default="ReadOnly" /></span>
	    <span class="property-value" aria-labelledby="readOnly-label"><g:fieldValue bean="${pxdItemObj}" field="readOnly"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.origin}">
	  <li class="fieldcontain">
	    <span id="origin-label" class="property-label"><g:message code="pxdItem.origin.label" default="Origin" /></span>
	    <span class="property-value" aria-labelledby="origin-label">
	      <g:link action="show" id="${pxdItemObj?.origin?.id}">${pxdItemObj?.origin?.path}</g:link>
	    </span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.uuid}">
	  <li class="fieldcontain">
	    <span id="uuid-label" class="property-label"><g:message code="pxdItem.uuid.label" default="Uuid" /></span>
	    <span class="property-value" aria-labelledby="uuid-label"><g:fieldValue bean="${pxdItemObj}" field="uuid"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.formDef}">
	  <li class="fieldcontain">
	    <span id="formDef-label" class="property-label"><g:message code="pxdItem.formDef.label" default="Form Def" /></span>
	    <span class="property-value" aria-labelledby="formDef-label"><g:fieldValue bean="${pxdItemObj}" field="formDef"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.dateCreated}">
	  <li class="fieldcontain">
	    <span id="dateCreated-label" class="property-label"><g:message code="pxdItem.createdUpdated.label" default="Date Created" /></span>
	    <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${pxdItemObj?.dateCreated}"/>/
	      <g:formatDate date="${pxdItemObj?.lastUpdated}"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.sha1}">
	  <li class="fieldcontain">
	    <span id="sha1-label" class="property-label"><g:message code="pxdItem.sha1.label" default="Sha1" /></span>
	    <span class="property-value" aria-labelledby="sha1-label"><g:fieldValue bean="${pxdItemObj}" field="sha1"/></span>
	  </li>
	</g:if>
	<g:if test="${pxdItemObj?.text}">
	  <li class="fieldcontain">
	    <span id="text-label" class="property-label"><g:message code="pxdItem.text.label" default="Text" /></span>
	    <span class="property-value" aria-labelledby="text-label"><g:fieldValue bean="${pxdItemObj}" field="text"/></span>
	  </li>
	</g:if>
      </ol>
      <g:form>
	<fieldset class="buttons">
	  <g:hiddenField name="id" value="${pxdItemObj?.id}" />
	  <g:link class="download" action="downloadContent" id="${pxdItemObj?.id}">
	    <g:message code="default.button.download.label" default="Download" /></g:link>
	  <g:if test="${pxdItemObj?.text}">
	    <g:link class="edit" action="edit" id="${pxdItemObj?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
	  </g:if>
	  <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
	</fieldset>
      </g:form>
    </div>
  </body>
</html>
