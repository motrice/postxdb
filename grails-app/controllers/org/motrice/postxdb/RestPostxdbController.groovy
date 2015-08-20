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

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*

/**
 * REST interface for reading the database according to the postxdb data model.
 * URL mappings begin with /postxdb/...
 * See BootStrap for XML conversion init.
 */
class RestPostxdbController {
  // Service injection
  def postxdbService
  def restService
  // For accessing the config
  def grailsApplication

  static CONT_DISP = 'Content-Disposition'

  /**
   * Get form definition metadata (PxdFormdef).
   * Null id lists all form definitions.
   */
  def formdefget(Long id) {
    if (log.debugEnabled) log.debug "DEFGET(${id}): ${Util.clean(params)}, ${request.forwardURI}"
    def formdefList = postxdbService.formdefGet(id)
    if (formdefList) {
      response.status = 200
      render(text: formdefList as XML, encoding:'UTF-8')
    } else {
      render(status: 404, text: 'No formdef was found', contentType: 'text/plain')
    }
  }

  /**
   * Get form definition version metadata (PxdFormdefVer).
   * If id is null exactly one of uuid and formdef parameters is required.
   */
  def versionget(Long id) {
    if (log.debugEnabled) log.debug "VERGET(${id}): ${Util.clean(params)}, ${request.forwardURI}"
    def map = postxdbService.formdefVerGet(id, params.uuid, params.formdef)
    response.status = map.status
    if (map.status == 200) {
      render(text: map.list as XML, encoding:'UTF-8')
    } else {
      render(contentType: 'text/plain', text: map.message)
    }
  }

  /**
   * Get a list of form definition items (a list of item ids).
   * Exactly one of uuid and formdef parameters is required.
   */
  def defitemget() {
    if (log.debugEnabled) log.debug "DEFITEM: ${Util.clean(params)}, ${request.forwardURI}"
    def map = postxdbService.defItemGet(params.uuid, params.formdef)
    response.status = map.status
    if (map.status == 200) {
      map.list.each {item -> item.formref = map.formref}
      render(text: map.list as XML, encoding:'UTF-8')
    } else {
      render(contentType: 'text/plain', text: map.message)
    }
  }

  /**
   * Get a list of form instance items (a list of item ids).
   * The formdefver parameter is required.
   */
  def institemget() {
    if (log.debugEnabled) log.debug "INSTITEM: ${Util.clean(params)}, ${request.forwardURI}"
    def itemList = postxdbService.instItemGet(params.formdefver)
    if (itemList) {
      response.status = 200
      render(text: itemList as XML, encoding:'UTF-8')
    } else {
      render(status: 404, text: 'No item found', contentType: 'text/plain')
    }
  }

  /**
   * Get a an item, i.e. its contents.
   */
  def itemgetbyid(Long id) {
    if (log.debugEnabled) log.debug "ITEMBYID(${id}): ${Util.clean(params)}, ${request.forwardURI}"
    def item = postxdbService.itemGet(id)
    if (item) {
      def fileName = item.path.replace('/', '#')
      String contDisp = "attachment;filename=${fileName}"
      if (item.xmlFormat()) {
	response.setHeader(CONT_DISP, contDisp)
	render(status: 200, text: item.text, contentType: 'application/xml', encoding: 'UTF-8')
      } else {
	response.setHeader(CONT_DISP, contDisp)
	response.status = 200
	response.contentType = 'application/octet-stream'
	response.getOutputStream().withStream {stream ->
	  stream.bytes = item.stream
	}
      }
    } else {
      response.status = 404
    }
  }

  /**
   * Get the read-only status of a form instance.
   * GET $POSTXDB/readonly/$uuid
   * NOTE: Only checks the XML item.
   */
  def readonlyget() {
    if (log.debugEnabled) log.debug "READONLY_GET: ${Util.clean(params)}, ${request.forwardURI}"
    def item = postxdbService.instItemGetXml(params.uuid)
    if (item) {
	render (status: 200, contentType: 'application/xml;charset=UTF-8') {
	  pxdItem {
	    uuid(params.uuid)
	    path(item.path)
	    readOnly(new Boolean(item.readOnly))
	  }
	}
    } else {
      response.status = 404
    }
  }

  /**
   * Set the read-only status of a form instance.
   * PUT $POSTXDB/readonly/$uuid[?ro=true|false]
   * Sets all items belonging to this instance to the given read-only status.
   * The default is ro=true.
   */
  def readonlyset(String ro) {
    if (log.debugEnabled) log.debug "READONLY_SET: ${Util.clean(params)}, ${request.forwardURI}"
    String instanceUuid = params.uuid
    def items = postxdbService.instItemGetAll(instanceUuid)
    if (items) {
      try {
	boolean roState = 'false'.equalsIgnoreCase(params.ro)? false : true
	def count = postxdbService.makeInstanceReadOnly(items, roState)
	render (status: 200, contentType: 'application/xml;charset=UTF-8') {
	  list(size: count) {
	    items.each {item ->
	      pxdItem {
		uuid(item.uuid)
		path(item.path)
		readOnly(item.readOnly)
	      }
	    }
	  }
	}
      } catch (PostxdbException exc) {
	log.error(exc.message)
	response.status = exc.http
	render(status: exc.http, contentType: 'text/plain', text: message(code: exc.code))
      }
    } else {
      render(status: 404)
    }
  }

  /**
   * Duplicate a form instance, i.e. all its items.
   * By default set the source instance read-only.
   * PUT $POSTXDB/duplicate/$srcuuid/$tgtuuid
   */
  def duplicateinstance(String ro) {
    if (log.debugEnabled) log.debug "DUPLICATE: ${Util.clean(params)}, ${request.forwardURI}"
    String srcuuid = params.srcuuid
    String tgtuuid = params.tgtuuid
    boolean readOnly = 'false'.equalsIgnoreCase(params.ro)? false : true
    def item = postxdbService.instItemGetXml(srcuuid)

    if (item) {
      try {
	def itemList = [postxdbService.duplicateInstance(item, tgtuuid, readOnly)]
	render(status: 200, text: itemList as XML, encoding:'UTF-8')
      } catch (RuntimeException exc) {
	response.status = 409
	render exc.errors as XML
      }
    } else {
      render(status: 404)
    }
  }

  /**
   * Add a language to a form.
   * Begins with the current draft, creates a new draft that becomes current.
   * PUT $POSTXDB/language/$app/$form?lang=$language-spec
   */
  def languageadd(String spec) {
    if (log.debugEnabled) log.debug "ADD LANG << ${Util.clean(params)}, ${request.forwardURI}"
    def enableProp = grailsApplication.config.postxdb.rest.allowAddLanguage
    boolean enableFlag = enableProp == 'true'
    if (enableFlag && spec) {
      try {
	def langMap = restService.addLanguage(params.app, params.form, spec)
	def writer = new StringWriter()
	def xml = new groovy.xml.MarkupBuilder(writer)
	xml.languages {
	  added {
	    langMap.added.each {langName ->
	      language(langName)
	    }
	  }
	  available {
	    langMap.available.each {langName ->
	      language(langName)
	    }
	  }
	}
	render(status: 201, contentType: 'text/xml;charset=UTF-8', text: writer.toString())
      } catch (PostxdbException exc) {
	if (log.debugEnabled) log.debug "add lang CONFLICT: ${exc.message}"
	// Do it the right way to get XML escaping
	def writer = new StringWriter()
	def xml = new groovy.xml.MarkupBuilder(writer)
	xml.conflict(exc.message)
	render(status: exc.http, contentType: 'text/xml;charset=UTF-8',	text: writer.toString())
      }
    } else {
      if (log.debugEnabled) log.debug "add lang NO-OP: enable=${enableFlag}, lang-spec=${spec}"
      def writer = new StringWriter()
      def xml = new groovy.xml.MarkupBuilder(writer)
      xml.conflict('POSTXDB.114|Not enabled or no language spec')
      render(status: 200, contentType: 'text/xml;charset=UTF-8', text: writer.toString())
    }
  }

  /**
   * List available languages in a form.
   * GET $POSTXDB/language/$app/$form[?version=$version]
   */
  def languagelist(String version) {
    if (log.debugEnabled) log.debug "GET LANG << ${Util.clean(params)}, ${request.forwardURI}"
    try {
      def langList = restService.findAvailableLanguages(params.app, params.form, version)
      def writer = new StringWriter()
      def xml = new groovy.xml.MarkupBuilder(writer)
      xml.languages {
	langList.each {langName ->
	  language(langName)
	}
      }
      render(status: 200, contentType: 'text/xml;charset=UTF-8', text: writer.toString())
    } catch (PostxdbException exc) {
      if (log.debugEnabled) log.debug "add lang CONFLICT: ${exc.message}"
      // Do it the right way to get XML escaping
      def writer = new StringWriter()
      def xml = new groovy.xml.MarkupBuilder(writer)
      xml.conflict(exc.message)
      render(status: exc.http, contentType: 'text/xml;charset=UTF-8', text: writer.toString())
    }
  }

  def putnamevalue() {
    if (log.debugEnabled) log.debug "PUT NVARRAY << ${Util.clean(params)}, ${request.forwardURI}"
    def item = null
    try {
      item = restService.createFormdataFromArray(params.app, params.form, params.version, request.reader.text)
      render(status: 201, contentType: 'text/plain;charset=UTF-8', text: item.uuid)
    } catch (PostxdbException exc) {
      if (log.debugEnabled) log.debug "put nvarray CONFLICT: ${exc.message}"
      // Do it the right way to get XML escaping
      def writer = new StringWriter()
      def xml = new groovy.xml.MarkupBuilder(writer)
      xml.conflict(exc.message)
      render(status: exc.http, contentType: 'text/xml;charset=UTF-8', text: writer.toString())
    }
  }

}
