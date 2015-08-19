<%-- == Motrice Copyright Notice ==

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
<%@ page import="org.motrice.postxdb.PxdFormdef" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
      <g:set var="entityName" value="${message(code: 'pxdFormdef.label', default: 'PxdFormdef')}" />
      <title><g:message code="default.list.label" args="[entityName]" /></title>
    </meta>
  </head>
  <body>
    <a href="#list-pxdFormdef" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div id="list-pxdFormdef" class="content scaffold-list" role="main">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
      </g:if>
      <table>
	<g:set var="withOrbeon" value="${frNew != null && fbEdit != null}"/>
	<thead>
	  <tr>
	    <g:sortableColumn property="path" title="${message(code: 'pxdFormdef.path.label', default: 'Path')}" />
	    <g:if test="${withOrbeon}">
	      <th><g:message code="pxdFormdef.apply.label"/></th>
	    </g:if>
	    <th><g:message code="pxdFormdef.latest.published.title"/></th>
	    <g:sortableColumn property="lastUpdated" title="${message(code: 'pxdFormdef.lastUpdated.label', default: 'Last Updated')}" />
	    <g:sortableColumn property="uuid" title="${message(code: 'pxdFormdef.uuid.label', default: 'Uuid')}" />
	    <g:if test="${withOrbeon}">
	      <th><g:message code="default.button.edit.label"/></th>
	    </g:if>
	  </tr>
	</thead>
	<tbody>
	  <g:each in="${pxdFormdefObjList}" status="i" var="pxdFormdefObj">
	    <g:set var="latestPublished" value="${org.motrice.postxdb.PxdFormdefVer.latestPublished(pxdFormdefObj)}"/>
	    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
	      <td><g:link action="show" id="${pxdFormdefObj.id}">${fieldValue(bean: pxdFormdefObj, field: "path")}</g:link></td>
	      <g:if test="${withOrbeon}">
		<g:set var="frNewUrl" value="${frNew(pxdFormdefObj)}"/>
		<g:if test="${frNewUrl}">
		<td><a href="${frNewUrl}" target="_"><g:img directory="images" file="page_white_add.png" title="${message(code: 'pxdFormdef.apply.tip')}"/></a></td>
		</g:if><g:else><td>--</td></g:else>
	      </g:if>
	      <g:if test="${latestPublished}">
		<td><g:link controller="pxdFormdefVer" action="show" id="${latestPublished.id}">${latestPublished.versionDisplay}</g:link>
	      </td></g:if>
	      <g:else><td>--</td></g:else>
	      <td><g:formatDate date="${pxdFormdefObj.lastUpdated}" /></td>
	      <td><g:abbr text="${pxdFormdefObj.uuid}"/></td>
	      <g:if test="${withOrbeon}">
		<td><a href="${fbEdit(pxdFormdefObj)}" target="_"><g:img directory="images" file="database_edit.png" title="${message(code: 'pxdFormdef.edit.tip')}"/></a></td>
	      </g:if>
	    </tr>
	  </g:each>
	</tbody>
      </table>
      <div class="pagination">
	<g:paginate total="${pxdFormdefObjTotal}" />
      </div>
    </div>
  </body>
</html>
