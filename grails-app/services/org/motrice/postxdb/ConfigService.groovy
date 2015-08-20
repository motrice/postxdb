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

// The only way to create a logger with a predictable name
import org.apache.commons.logging.LogFactory

/**
 * Configuration-related methods.
 */
class ConfigService {
  private static final log = LogFactory.getLog(this)

  // No database stuff in this service
  static transactional = false

  def grailsApplication

  // Datasource injected to allow direct SQL query
  javax.sql.DataSource dataSource
  
  // States of configured resources
  static final STATE_CONFIGURED = 1
  static final STATE_NOT_CONFIGURED = 2
  static final STATE_OPERATIONAL = 3
  static final STATE_INOPERATIVE = 4
  static final STATE_DIAGNOSTIC = 5

  /**
   * Return a URL for creating a new form in Form Builder, or the empty string if Orbeon is
   * not configured.
   */
  String formBuilderNew() {
    def result = ''
    def baseUrl = grailsApplication.config.postxdb.orbeon.urlBase
    if (baseUrl) {
      result = "${baseUrl}/orbeon/builder/new"
    }

    return result
  }

  /**
   * Return a closure expecting a PxdFormdef parameter, or null if Orbeon is not configured.
   * The closure, when called, returns a String, the URL to invoke Form Builder with
   * the PxdFormdef (its current draft), or null if the item is not a form definition.
   */
  def formBuilderEdit() {
    def result = null
    def baseUrl = grailsApplication.config.postxdb.orbeon.urlBase
    if (baseUrl) {
      result = {form ->
	boolean cond = form && (form instanceof org.motrice.postxdb.PxdFormdef)
	cond? "${baseUrl}/orbeon/builder/edit/${form.uuid}" : null
      }
    }

    return result
  }

  /**
   * Return a closure expecting a PxdFormdef parameter, or null if Orbeon is not configured.
   * The closure, when called, returns a String, the URL to invoke Form Runner to create
   * a new form instance from the PxdFormdef.
   * The closure returns null if the item is not a form definition or if it has no published
   * version.
   */
  def formRunnerNew() {
    def result = null
    def baseUrl = grailsApplication.config.postxdb.orbeon.urlBase
    if (baseUrl) {
      result = {form ->
	def url = null
	if (form && (form instanceof org.motrice.postxdb.PxdFormdef)) {
	  def latest = PxdFormdefVer.latestPublished(form)
	    url = latest? "${baseUrl}/${latest.path}/new" : null
	  }
	
	return url
      }
    }

    return result
  }

  /**
   * Return a closure expecting a PxdItem parameter, or null if Orbeon is not configured.
   * The closure, when called, returns a String, the URL to invoke Form Runner with
   * the PxdItem, or null if the item is not a form instance with xml format.
   */
  def formRunnerEdit() {
    def result = null
    def baseUrl = grailsApplication.config.postxdb.orbeon.urlBase
    if (baseUrl) {
      result = {item ->
	boolean cond = item && (item instanceof org.motrice.postxdb.PxdItem) &&
	item.instance && item.xmlFormat()
	cond? "${baseUrl}/${item.formDef}/edit/${item.uuid}" : null
      }
    }

    return result
  }

  /**
   * Create a list of important configuration settings, some with liveness
   */
  List configDisplay() {
    def list = []
    // If you ask for a non-configured configuration item the response is an empty Map.
    // Use ?: to convert to null and avoid complications.
    list << configItem('config.item.applicationName',
		       grailsApplication.metadata.'app.name' ?: null)
    list << configItem('config.item.applicationVersion',
		       grailsApplication.metadata.'app.version' ?: null)
    list << configItem('config.item.allowAddLanguage',
		       grailsApplication.config.postxdb.gui.allowAddLanguage ?: null)
    list << configItem('config.item.callback.sourcePort',
		       grailsApplication.config.postxdb.callback.source.port ?: null)
    list << configItem('config.item.callback.destPort',
		       grailsApplication.config.postxdb.callback.destination.port ?: null)
    def item = configItem('config.item.dataSource',
			  grailsApplication.config.dataSource.url ?: null)
    dataSourceLive(list, item)
    item = configItem('config.item.orbeonBaseUri',
		      grailsApplication.config.postxdb.orbeon.urlBase ?: null)
    orbeonLive(list, item)

    return list
  }

  private Map configItem(String resourceName, String value) {
    def item = [name: resourceName, value: value]
    if (item.value) {
      item.state = STATE_CONFIGURED
    } else {
      item.value = '--'
      item.state = STATE_NOT_CONFIGURED
    }

    return item
  }

  private Map problemItem(String message) {
    [name: 'config.liveness.problem', value: message, state: STATE_DIAGNOSTIC]
  }

  /**
   * Check if the datasource is live.
   * item must be the result of configItem
   * SIDE EFFECT: Adds one or two config items (Map) to the list.
   */
  private dataSourceLive(List list, Map item) {
    if (item.state == STATE_CONFIGURED) {
      def cnx = null
      def rs = null
      try {
	cnx = dataSource.connection
	def meta = cnx.metaData
	def tableNames = []
	rs = meta.getTables(null, null, 'pxd_%', null)
	while (rs.next()) {
	  tableNames.add(rs.getString(3))
	}
	if (tableNames.size() >= 3) {
	  item.state = STATE_OPERATIONAL
	  list << item
	} else {
	  item.state = STATE_INOPERATIVE
	  list << item
	  list << problemItem("Tables found: ${tableNames}")
	}
      } catch (Exception exc) {
	item.state = STATE_INOPERATIVE
	list << item
	list << problemItem(exc.message)
      } finally {
	try {
	  rs?.close()
	} catch (Exception exc2) {
	  // Ignore
	}
	try {
	  cnx?.close()
	} catch (Exception exc2) {
	  // Ignore
	}
      }
    } else {
      list << item
    }
  }

  /**
   * Check orbeon liveness.
   * item must be the result of configItem
   * SIDE EFFECT: Adds one or two config items (Map) to the list.
   */
  private orbeonLive(List list, Map item) {
    if (item.state == STATE_CONFIGURED) {
      try {
	String urlStr = "${item.value}/orbeon/builder/summary"
	def url = new URL(urlStr)
	def summary = url.getText('UTF-8')
	item.state = (summary?.length() > 0)? STATE_OPERATIONAL : STATE_INOPERATIVE
	list << item
      } catch (Exception exc) {
	item.state = STATE_INOPERATIVE
	list << item
	list << problemItem(exc.message)
      }
    } else {
      list << item
    }
  }

}
