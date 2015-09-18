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
<div class="mainmenu" role="navigation">
  <ul id="menu">
      <li><g:link controller="pxdFormdef" action="list"><g:message code="menu.forms"/></g:link>
	<ul class="submenu">
	  <li><g:link controller="pxdFormdef" action="list"><g:message code="pxdFormdef.list.menu"/></g:link></li>
	  <g:if test="${formBuilderNewUrl}">
	  <li><a href="${formBuilderNewUrl}" target="_"><g:message code="pxdFormdef.new.menu"/></a></li>
	  </g:if>
	</ul>
      </li>
      <li><g:link controller="pxdItem" action="list"><g:message code="menu.contents"/></g:link>
	<ul class="submenu">
	  <li><g:link controller="pxdItem" action="list"><g:message code="pxdItem.listAll.menu"/></g:link></li>
	  <li><g:link controller="pxdItem" action="listInst"><g:message code="pxdItem.listInstances.menu"/></g:link></li>
	</ul>
      </li>
      <li><a class="compact" href="${createLink(uri: '/')}"><g:img dir="images" file="gearbox35.jpg"/></a>
	<ul class="submenu">
	  <li><g:link controller="pxdFormdef" action="showconfig"><g:message code="config.show.menu"/></g:link></li>
	</ul>
      </li>
  </ul>
</div>
