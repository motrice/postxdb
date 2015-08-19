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
