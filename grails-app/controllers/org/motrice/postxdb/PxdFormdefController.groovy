/* == Motrice Copyright Notice ==
 *
 * Motrice Service Platform
 *
 * Copyright (C) 2011-2014 Motrice AB
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
 * mail: Motrice AB, Långsjövägen 8, SE-131 33 NACKA, SWEDEN
 * phone: +46 8 641 64 14
 */
package org.motrice.postxdb

import org.motrice.postxdb.PxdFormdef;
import org.springframework.dao.DataIntegrityViolationException

class PxdFormdefController {

  def grailsApplication
  def restService

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def index() {
    redirect(action: "list", params: params)
  }

  def list(Integer max) {
    params.max = Math.min(max ?: 10, 100)
    def dbUrl = grailsApplication.config.dataSource.url
    [pxdFormdefObjList: PxdFormdef.list(params), pxdFormdefObjTotal: PxdFormdef.count(), dbUrl: dbUrl]
  }

  def create() {
    [pxdFormdefObj: new PxdFormdef(params)]
  }

  def save() {
    def pxdFormdefObj = new PxdFormdef(params)
    if (!pxdFormdefObj.save(flush: true)) {
      render(view: "create", model: [pxdFormdefObj: pxdFormdefObj])
      return
    }

    flash.message = message(code: 'default.created.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), pxdFormdefObj.id])
    redirect(action: "show", id: pxdFormdefObj.id)
  }

  def addLanguages(Long id) {
    def pxdFormdefObj = PxdFormdef.get(id)
    if (!pxdFormdefObj) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
      return
    }

    [pxdFormdefObj: pxdFormdefObj]
  }

  def commitLanguages(AddLanguagesCommand cmd) {
    if (log.debugEnabled) log.debug "COMMIT LANGUAGES: ${params} ${cmd}"
    def formdef = PxdFormdef.get(params.id)
    def enableProp = grailsApplication.config.postxdb.allow.gui.add.language
    boolean enableFlag = enableProp == 'true'

    if (enableFlag) {
      try {
	def langList = restService.addLanguage(formdef.appName, formdef.formName, cmd.langSpec)
	flash.message = message(code: 'pxdFormdef.addLanguage.created',
	args: [langList?.toString()])
      } catch (PostxdbException exc) {
	flash.message = exc.message
      }
    } else {
      flash.message = message(code: 'pxdFormdef.addLanguage.disabled')
    }

    redirect(action: "show", id: params.id)
  }

  def show(Long id) {
    def pxdFormdefObj = PxdFormdef.get(id)
    if (!pxdFormdefObj) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
      return
    }

    def enableProp = grailsApplication.config.postxdb.allow.gui.add.language
    Boolean enableFlag = enableProp == 'true'
    [pxdFormdefObj: pxdFormdefObj, addLangEnable: enableFlag]
  }

  def edit(Long id) {
    def pxdFormdefObj = PxdFormdef.get(id)
    if (!pxdFormdefObj) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
      return
    }

    [pxdFormdefObj: pxdFormdefObj]
  }

  def update(Long id, Long version) {
    def pxdFormdefObj = PxdFormdef.get(id)
    if (!pxdFormdefObj) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
      return
    }

    if (version != null) {
      if (pxdFormdefObj.version > version) {
	pxdFormdefObj.errors.rejectValue("version", "default.optimistic.locking.failure",
					 [message(code: 'pxdFormdef.label', default: 'PxdFormdef')] as Object[],
					 "Another user has updated this PxdFormdef while you were editing")
	render(view: "edit", model: [pxdFormdefObj: pxdFormdefObj])
	return
      }
    }

    pxdFormdefObj.properties = params

    if (!pxdFormdefObj.save(flush: true)) {
      render(view: "edit", model: [pxdFormdefObj: pxdFormdefObj])
      return
    }

    flash.message = message(code: 'default.updated.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), pxdFormdefObj.id])
    redirect(action: "show", id: pxdFormdefObj.id)
  }

  def delete(Long id) {
    def pxdFormdefObj = PxdFormdef.get(id)
    if (!pxdFormdefObj) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
      return
    }

    try {
      pxdFormdefObj.delete(flush: true)
      flash.message = message(code: 'default.deleted.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "list")
    }
    catch (DataIntegrityViolationException e) {
      flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'pxdFormdef.label', default: 'PxdFormdef'), id])
      redirect(action: "show", id: id)
    }
  }
}

class AddLanguagesCommand {
  String srcLang
  String tgtLang

  def String getSrcLang() {
    srcLang?.trim()
  }

  def String getFormatTgtLang() {
    def langList = []
    langList.addAll(tgtLang.trim().split(/\s/))
    return langList.join('.')
  }

  def String getLangSpec() {
    "${srcLang}~${formatTgtLang}"
  }

  String toString() {
    "[AddLang src:${srcLang} tgt:${tgtLang}]"
  }

}