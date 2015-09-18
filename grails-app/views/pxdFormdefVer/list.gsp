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
<%@ page import="org.motrice.postxdb.PxdFormdefVer" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
      <g:set var="entityName" value="${message(code: 'pxdFormdefVer.label', default: 'PxdFormdefVer')}" />
      <title><g:message code="default.list.label" args="[entityName]" /></title>
    </meta>
  </head>
  <body>
    <a href="#list-pxdFormdefVer" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div id="list-pxdFormdefVer" class="content scaffold-list" role="main">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
      </g:if>
      <table>
	<thead>
	  <tr>
	    <g:sortableColumn property="path" title="${message(code: 'pxdFormdefVer.path.label', default: 'Path')}" />
	    <g:sortableColumn property="title" title="${message(code: 'pxdFormdefVer.title.label', default: 'Title')}" />
	    <g:sortableColumn property="dateCreated" title="${message(code: 'pxdFormdefVer.dateCreated.label', default: 'Date Created')}" />
	    <g:sortableColumn property="lastUpdated" title="${message(code: 'pxdFormdefVer.lastUpdated.label', default: 'Date Created')}" />
	  </tr>
	</thead>
	<tbody>
	  <g:each in="${pxdFormdefVerObjList}" status="i" var="pxdFormdefVerObj">
	    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
	      <td><g:link action="show" id="${pxdFormdefVerObj.id}">${fieldValue(bean: pxdFormdefVerObj, field: "path")}</g:link></td>
	      <td>${fieldValue(bean: pxdFormdefVerObj, field: "title")}</td>
	      <td><g:formatDate date="${pxdFormdefVerObj.dateCreated}" /></td>
	      <td><g:formatDate date="${pxdFormdefVerObj.lastUpdated}" /></td>
	    </tr>
	  </g:each>
	</tbody>
      </table>
      <div class="pagination">
	<g:paginate total="${pxdFormdefVerObjTotal}" />
      </div>
    </div>
  </body>
</html>
