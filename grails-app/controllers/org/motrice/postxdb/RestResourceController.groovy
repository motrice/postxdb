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

import org.motrice.postxdb.Util;
import org.springframework.dao.DataIntegrityViolationException

/**
 * REST interface for saved form definitions and some associated items, especially logo.
 * Saved means they are stored under "orbeon/builder".
 * These form definitions become drafts.
 * Putting and getting use a uuid as a directory name (called catalog in eXist).
 * The real app/form names are buried in form definition XML.
 * We map these form definitions to our data model.
 * Orbeon will still access this stuff by means of an uuid.
 * Orbeon keeps track of uuids in the session so we must use them and nothing else.
 * URL mapping:
 * "/rest/db/orbeon-pe/fr/orbeon/builder/data/$uuid/$resource"(controller: 'RestResource') {
 *   action = [GET: 'getop', PUT: 'putop', DELETE: 'delete']
 * }
 */
class RestResourceController {
  // RestService injection
  def restService

  /**
   * Get a resource, XML or binary
   */
  def getop() {
    if (log.debugEnabled) log.debug "GETOP: ${Util.clean(params)}, ${request.forwardURI}"
    def itemObj = null
    if (params?.resource?.startsWith('data')) {
      itemObj = restService.findCurrentDraft(params.uuid)
    } else {
      itemObj = restService.findFormdefResource(params.uuid, params.resource)
    }

    // Return the contents
    if (itemObj) {
      if (log.debugEnabled) log.debug "getop FOUND: ${itemObj}"
      if (itemObj.xmlFormat()) {
	render(text: itemObj.text, contentType: 'application/xml;charset=UTF-8', encoding: 'UTF-8')
      } else {
	response.status = 200
	response.contentType = 'application/octet-stream'
	// response.contentLength = itemObj.size
	response.getOutputStream().withStream {stream ->
	  stream.bytes = itemObj.stream
	}
      }
    } else {
      if (log.infoEnabled) log.info "getop >> 404"
      render(status: 404, text: 'Item was not found', contentType: 'text/plain')
    }
  }

  /**
   * Save a resource, XML or binary
   * If the resource is not a form definition its name is another uuid plus file extension.
   * As a rule, Orbeon saves binary stuff (logo etc) before saving the form definition.
   */
  def putop() {
    if (log.debugEnabled) log.debug "PUTOP: ${Util.clean(params)}, ${request.forwardURI}"
    def itemObj = null
    // Two distinct flows depending on the format
    if (request.format == 'xml') {
      if (log.debugEnabled) log.debug "putop XML >> createDraftItem"
      itemObj = restService.createDraftItem(params.uuid, params.resource, request.reader.text)
      if (log.debugEnabled) log.debug "putop XML << ${itemObj}"
    } else {
      if (log.debugEnabled) log.debug "putop BIN >> createFormdefResource"
      itemObj = restService.createFormdefResource(params.uuid, params.resource, request)
      if (log.debugEnabled) log.debug "putop BIN << ${itemObj}"
    }

    if (itemObj) {
      restService.addPathHeader(itemObj, response)
      // Too bad, but Orbeon chokes if we return anything, response must be empty.
      render(status: 201)
    } else {
      String msg = 'CONFLICT PxdItem not found'
      log.warn "putop ${msg}"
      render(status: 409, text: msg, contentType: 'text/plain')
    }
  }

  /**
   * Delete a form definition resource.
   */
  def delete() {
    if (log.debugEnabled) log.debug "DELETE (no-op): ${params}, ${request.forwardURI}"
  }
}
