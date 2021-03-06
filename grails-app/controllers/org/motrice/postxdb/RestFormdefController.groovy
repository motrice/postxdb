/* == Motrice Copyright Notice ==
 *
 * Motrice BPM
 *
 * Copyright (C) 2011-2015 Motrice AB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * e-mail: info _at_ motrice.se
 * mail: Motrice AB, Halmstadsvägen 16, SE-121 51 JOHANNESHOV, SWEDEN
 * phone: +46 73 341 4983
 */
package org.motrice.postxdb

import org.motrice.postxdb.PxdItem;
import org.motrice.postxdb.Util;
import org.springframework.dao.DataIntegrityViolationException

import org.motrice.postxdb.FormdefPath;

/**
 * REST interface for published form definitions.
 * URL mappings:
 * "/rest/db/orbeon-pe/fr/$app/$form/form/$resource"(controller: 'RestFormdef') {
 *   action = [GET: 'getop', PUT: 'putop', DELETE: 'delete']
 * }
 *"/rest/db/orbeon-pe/fr/orbeon/builder/data"(controller: 'RestFormdef', action: 'list')
 */
class RestFormdefController {
  // RestService injection
  def restService

  /**
   * Generate a list of forms available for editing.
   * For every form only the current draft is editable.
   * RETURNS an XML document.
   */
  def list() {
    def sessionId = g.cookie(name: 'JSESSIONID')
    if (log.debugEnabled) log.debug "LIST: ${Util.clean(params)}, ${request.forwardURI}, session ${sessionId}"
    Integer max = params?.'page-size' as Integer
    if (!max || max <= 0 || max > 100) max = 15
    Integer page = params?.'page-number' as Integer
    if (!page || page < 1 || page > 999) page = 1
    Integer offset = (page - 1) * max

    // The request may be a plain list or a search
    def formInfo = null
    if (params?.value) {
      // The request comes with search parameters
      def map = restService.extractSearchParameters(params.value, params?.path)
      formInfo = restService.formSearch(map)
    } else {
      // This is a plain list
      formInfo = restService.findEditableForms(max, offset)
    }
    def list = formInfo.list
    def total = formInfo.total
    render(status: 200, contentType: 'application/xml;charset=UTF-8') {
      "exist:result"("xmlns:exist": "http://exist.sourceforge.net/NS/exist", "exist:hits": "1",
      "exist:start": "1", "exist:count": "1") {
	documents(total: total, 'search-total': total, 'page-size': max, 'page-number': page, query: "")
	{
	  for (doc in list) {
	    def path = new FormdefPath(doc.path)
	    // The value of the 'name' attribute must be the uuid
	    document(created: doc.createdf(), 'last-modified': doc.updatedf(), draft: "false",
	    name: doc?.formdef?.uuid)
	    {
	      details {
		detail(doc.appName)
		detail(path.toString(false))
		detail(doc.title ?: '--')
		detail(doc.description ?: '--')
	      }
	    }
	  }
	}
      }
    }

  }

  /**
   * Get a resource for a published form definition version.
   * Currently only form definition itself, an xml document.
   */
  def getop() {
    def sessionId = g.cookie(name: 'JSESSIONID')
    if (log.debugEnabled) log.debug "GETOP: ${Util.clean(params)}, ${request.forwardURI}, session ${sessionId}"
    def path = new FormdefPath("${params?.app}/${params?.form}")
    def itemObj = null
    if (params?.resource == 'form.xhtml') {
      if (path.library) {
	itemObj = restService.findLibraryForm(path)
      } else {
	String itemPath = "${path}/form.xhtml"
	itemObj = PxdItem.findByPath(itemPath)
      }
    } else {
      itemObj = PxdItem.findByPath(params?.resource)
    }

    if (itemObj) {
      if (log.debugEnabled) log.debug "getop FOUND: ${itemObj}"
      if (itemObj.xmlFormat()) {
	render(text: itemObj.text, contentType: 'application/xml;charset=UTF-8', encoding: 'UTF-8')
      } else {
	response.status = 200
	response.contentType = 'application/octet-stream'
	//response.contentLength = itemObj.size
	response.getOutputStream().withStream {stream ->
	  stream.bytes = itemObj.stream
	}
      }
    } else {
      if (log.infoEnabled) log.info "getop item 404 (NOT FOUND): ${params?.app}/${params?.form}"
      render(status: 404, text: 'Item was not found', contentType: 'text/plain')
    }
  }

  /**
   * Publish and store a form definition resource.
   * Usually called by the form editor.
   * Currently only the form definition itself, an xml document, is stored this way.
   * If this is the first form definition version a new Formdef is created.
   */
  def putop() {
    def sessionId = g.cookie(name: 'JSESSIONID')
    if (log.debugEnabled) log.debug "PUTOP: ${Util.clean(params)}, ${request.forwardURI}, session ${sessionId}"
    def itemObj = null
    def msg = null
    // Two distinct flows depending on the input
    try {
      if (request.format == 'xml') {
	if (log.debugEnabled) log.debug "putop XML >> createPublishedItem"
	itemObj = restService.createPublishedItem(params.resource, request.reader.text)
	if (log.debugEnabled) log.debug "putop XML << ${itemObj}"
      } else {
	// Happens only in Orbeon 4
	if (log.debugEnabled) log.debug "putop BIN >> createPublishedResource"
	itemObj = restService.createPublishedResource(params.app, params.form, params.resource, request)
	if (log.debugEnabled) log.debug "putop BIN << ${itemObj}"
      }
    } catch (PostxdbException exc) {
      msg = message(code: exc.code)
    }

    if (itemObj) {
      restService.addPathHeader(itemObj, response)
      // The response must be empty, or Orbeon chokes
      render(status: 201)
    } else {
      if (!msg) msg = 'CONFLICT PxdItem not found'
      log.warn "putop ${msg}"
      render(status: 409, text: msg, contentType: 'text/plain')
    }
  }

  /**
   * Delete a form definition resource.
   */
  def delete() {
    if (log.debugEnabled) log.debug "DELETE (no-op): ${params}"
  }
}
