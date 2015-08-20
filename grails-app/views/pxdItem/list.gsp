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
<%@ page import="org.motrice.postxdb.PxdItem" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
      <g:set var="entityName" value="${message(code: 'pxdItem.label', default: 'PxdItem')}" />
      <title><g:message code="default.list.label" args="[entityName]" /></title>
    </meta>
  </head>
  <body>
    <a href="#list-pxdItem" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div id="list-pxdItem" class="content scaffold-list" role="main">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
      </g:if>
      <table>
	<thead>
	  <tr>
	    <g:sortableColumn property="path" title="${message(code: 'pxdItem.path.label', default: 'Path')}" />
	    <g:sortableColumn property="instance" title="${message(code: 'pxdItem.instance.label', default: 'Categ')}" />
	    <g:sortableColumn property="formDef" title="${message(code: 'pxdItem.formDef.label', default: 'Form Def')}" />
	    <g:sortableColumn property="lastUpdated" title="${message(code: 'pxdItem.lastUpdated.label', default: 'Timestamp')}" />
	    <g:sortableColumn property="uuid" title="${message(code: 'pxdItem.uuid.label', default: 'Uuid')}" />
	  </tr>
	</thead>
	<tbody>
	  <g:each in="${pxdItemObjList}" status="i" var="pxdItemObj">
	    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
	      <g:set var="editUrl" value="${frEdit? frEdit(pxdItemObj) : null}"/>
	      <g:if test="${editUrl}">
		<td><a href="${editUrl}" target="_">${fieldValue(bean: pxdItemObj, field: "path")}</a></td>
	      </g:if>
	      <g:else>
		<td>${fieldValue(bean: pxdItemObj, field: "path")}</td>
	      </g:else>
	      <td><g:instflag flag="${pxdItemObj?.instance}"/></td>
	      <td>${fieldValue(bean: pxdItemObj, field: "formDef")}</td>
	      <td><g:formatDate date="${pxdItemObj?.lastUpdated}"/></td>
	      <td><g:link action="show" id="${pxdItemObj.id}"><g:abbr text="${pxdItemObj?.uuid}"/></g:link></td>
	    </tr>
	  </g:each>
	</tbody>
      </table>
      <div class="pagination">
	<g:paginate total="${pxdItemObjTotal}" />
      </div>
    </div>
  </body>
</html>
