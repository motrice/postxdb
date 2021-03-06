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
// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

String generateConfigDefaultPath() {
  def ENV_VAR1 = 'POSTXDB_CONF'
  def ENV_VAR2 = 'MOTRICE_CONF'
  def FILENAME = '/usr/local/etc/motrice/motrice.properties'
  def CONFPROP1 = System.getProperty(ENV_VAR1)
  def CONFENV1 = System.getenv(ENV_VAR1)
  def CONFPROP2 = System.getProperty(ENV_VAR2)
  def CONFENV2 = System.getenv(ENV_VAR2)

  if (CONFPROP1) {
    println "--- Postxdb CONFIG: Command line specified ${CONFPROP1}"
    FILENAME = CONFPROP1
  } else if (CONFENV1) {
    println "--- Postxdb CONFIG: Environment specified ${CONFENV1}"
    FILENAME = CONFENV1
  } else if (CONFPROP2) {
    println "--- Postxdb CONFIG: Command line specified ${CONFPROP2}"
    FILENAME = CONFPROP2
  } else if (CONFENV2) {
    println "--- Postxdb CONFIG: Environment specified ${CONFENV2}"
    FILENAME = CONFENV2
  } else {
    def defaultFile = new File(FILENAME)
    if (defaultFile?.canRead()) {
      println "--- Postxdb CONFIG: Default ${FILENAME}"
    } else {
      println "--- Postxdb CONFIG: No config file available. Using the built-in H2 database."
    }
  }

  return FILENAME
}

String generateConfigEnvironmentPath(String defaultPath) {
  def GRAILSENV = grails.util.Environment.current.name
  def defaultConfig = new File(defaultPath)
  def envPath = "${defaultConfig.parent}/${GRAILSENV}-${defaultConfig.name}"
  def envConfig = new File(envPath)
  def result = null

  if (envConfig?.canRead()) {
    result = envPath
    println "--- Postxdb OVERRIDES in ${envPath}"
  } else {
    println "--- Postxdb: No [${GRAILSENV}] overrides"
  }

  return result
}

if (!grails.config.locations || !(grails.config.locations instanceof List)) {
  grails.config.locations = []
}
def configDefaultPath = generateConfigDefaultPath()
grails.config.locations << "file:${configDefaultPath}"
def configEnvironmentPath = generateConfigEnvironmentPath(configDefaultPath)
if (configEnvironmentPath) grails.config.locations << "file:${configEnvironmentPath}"

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Obj'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// Documentation properties
grails.doc.title = 'Postxdb Backend'
grails.doc.subtitle = 'Helpful Persistence for Orbeon Forms'
grails.doc.authors = 'Håkan Söderström, Motrice AB'

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'net.sf.ehcache.hibernate',
           'org.hibernate'

    error 'org.motrice.postxdb', 'grails.app.controllers', 'org.motrice.postxdb.RestService' , 'org.motrice.postxdb.PostxdbService', 'org.motrice.postxdb.ItemService', 'org.motrice.postxdb.ConfigService', 'org.motrice.postxdb.CallbackManager'
}

// Timestamp format in responses to Orbeon
postxdb.tstamp.fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS"
// Timestamp format in postxdb methods
postxdb.regular.fmt = "yyyy-MM-dd_HH:mm:ss"
// Header used to return item paths
postxdb.itempath.header = "X-Postxdb-Itempath"
// Base URL where Orbeon is running
postxdb.orbeon.urlBase = "http://localhost:8080/orbeon/fr"
