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
import grails.converters.*

class BootStrap {
  def callbackManager
  def grailsApplication

  def init = {servletContext ->
    // This call makes Grails look for a toXml method in the domain
    // when rendering an instance as XML
    XML.registerObjectMarshaller(new org.codehaus.groovy.grails.web.converters
   .marshaller.xml.InstanceMethodBasedMarshaller())

    // Start the callback manager
    callbackManager.startup()

    // Print the datasource url
    println "*** Datasource: ${grailsApplication.config.dataSource.url} ***"
  }

  def destroy = {
    callbackManager.shutdown()
  }

}
