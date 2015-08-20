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

import org.motrice.postxdb.PxdFormdef;
import org.springframework.dao.DataIntegrityViolationException

class PxdFormdefController {

  def grailsApplication
  def configService
  def restService

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def index() {
    redirect(action: "list", params: params)
  }

  def list(Integer max) {
    params.max = Math.min(max ?: 15, 100)
    [pxdFormdefObjList: PxdFormdef.list(params), pxdFormdefObjTotal: PxdFormdef.count(),
    frNew: configService.formRunnerNew(), fbEdit: configService.formBuilderEdit()]
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
    def enableProp = grailsApplication.config.postxdb.gui.allowAddLanguage
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

    def enableProp = grailsApplication.config.postxdb.gui.allowAddLanguage
    Boolean enableFlag = enableProp == 'true'
    [pxdFormdefObj: pxdFormdefObj, addLangEnable: enableFlag]
  }

  def showconfig() {
    // Display table for decoding resources
    def table = [:]
    // Configured
    def appName = grailsApplication.metadata.'app.name'
    table[1] = display("/${appName}/images/bullet_white.png", 'config.liveness.1')
    table[2] = display("/${appName}/images/exclamation.png", 'config.liveness.2')
    table[3] = display("/${appName}/images/tick.png", 'config.liveness.3')
    table[4] = display("/${appName}/images/cross.png", 'config.liveness.4')
    table[5] = display("/${appName}/images/comment.png", 'config.liveness.5')
    def configList = configService.configDisplay()
    // Replace stuff in the list
    def displayList = configList.collect {item ->
      def res = table[item.state]
      item.name = message(code: item.name)
      item.img = res.img
      item.title = message(code: res.title)
      return item
    }
    [config: displayList]
  }

  private Map display (String img, String title) {
    [img: img, title: title]
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
