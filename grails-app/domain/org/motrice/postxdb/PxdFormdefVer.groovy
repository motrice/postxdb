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

import java.text.SimpleDateFormat

import org.apache.commons.logging.LogFactory

import org.motrice.postxdb.FormdefMeta;
import org.motrice.postxdb.FormdefPath;

/**
 * A form definition version.
 * The FormdefPath class is used to do things with version numbers.
 * Orbeon does not recognize versions, but treats every version as an independent
 * form definition.
 * Tech note: Although the path is unique, the usual default id is used as primary key.
 * The reason is mainly complications for the non-REST part of the application
 * when deviating from standard Grails conventions.
 */
class PxdFormdefVer implements Comparable {
  // Injection magic
  def grailsApplication

  private static final log = LogFactory.getLog(this)

  /**
   * Query to find the latest non-withdrawn version of a form definition
   */
  static final String LASTVERQ =
    "from PxdFormdefVer as v where appName=? and formName=? and v.draft<${FormdefPath.WITHDRAWN} " +
    'order by v.fvno, v.draft desc'

  /**
   * Unique designation: appName/formName--vNNN[_DD]
   * Its components are stored with intentional redundancy to simplify SQL access.
   * Use the assignPath methods.
   * The appName/formName is redundant in itself, implicit in the relationship to Formdef.
   */
  String path

  /**
   * Application name. May not be modified.
   */
  String appName

  /**
   * Form name without version. May not be modified.
   */
  String formName

  /**
   * Form version number. May not be modified.
   */
  Integer fvno

  /**
   * Form draft number. May not be modified.
   * The FormdefPath class defines the details of version management.
   * The draft number is defined in a way to make sorting turn out right.
   * 9999 (magic number) means not-a-draft, the version has been published
   * If the version has been published the draft number is omitted from the
   * display form of the path.
   * 10000 (magic number) means the version has been explicitly withdrawn (unpublished)
   */
  Integer draft

  /**
   * Auto timestamping
   */
  Date dateCreated
  Date lastUpdated

  /**
   * Form title
   */
  String title

  /**
   * Form description
   */
  String description

  /**
   * Form language (two-letter abbreviation)
   */
  String language

  /**
   * Author (a piece of metadata not used by Orbeon)
   */
  String author

  /**
   * Url-like reference to the form logo
   */
  String logoRef

  static transients = ['currentDraft']
  static belongsTo = [formdef: PxdFormdef]
  static constraints = {
    path nullable: false, size: 3..255, unique: true
    appName size: 1..120
    formName size: 1..120
    fvno range: 1..9999
    draft range: 1..10000
    dateCreated nullable: true
    lastUpdated nullable: true
    title nullable: true, maxSize: 120
    description nullable: true, maxSize: 800
    language nullable: true, maxSize: 16
    author nullable: true, maxSize: 80
    logoRef nullable: true, maxSize: 800
    formdef nullable: false
  }

  /**
   * Convenience method for a common case
   */
  static PxdFormdefVer createInstance(FormdefPath fdpath) {
    def formdefVer = new PxdFormdefVer()
    formdefVer.assignPath(fdpath)
    return formdefVer
  }

  boolean isCurrentDraft() {
    path == formdef.currentDraft
  }

  /**
   * Set all components of the primary key
   */
  def assignPath(String path) {
    def fdpath = new FormdefPath(path)
    assignPath(fdpath)
  }

  def assignPath(FormdefPath fdpath) {
    this.path = fdpath.toString()
    appName = fdpath.appName
    formName = fdpath.formName
    fvno = fdpath.version
    draft = fdpath.draft
  }

  def assignMeta(FormdefMeta meta) {
    title = meta.title
    description = meta.description
    language = meta.language
    author = meta.author
    logoRef = meta.logo
  }

  /**
   * Formatted creation timestamp
   */
  String createdf() {
    def fmt = new SimpleDateFormat(grailsApplication.config.postxdb.tstamp.fmt)
    return fmt.format(dateCreated)
  }

  /**
   * Formatted updated timestamp
   */
  String updatedf() {
    def fmt = new SimpleDateFormat(grailsApplication.config.postxdb.tstamp.fmt)
    return fmt.format(lastUpdated)
  }

  /**
   * Creation timestamp regular format
   */
  String createdr() {
    def fmt = new SimpleDateFormat(grailsApplication.config.postxdb.regular.fmt)
    return fmt.format(dateCreated)
  }

  /**
   * Updated timestamp regular format
   */
  String updatedr() {
    def fmt = new SimpleDateFormat(grailsApplication.config.postxdb.regular.fmt)
    return fmt.format(lastUpdated)
  }

  boolean isPublished() {
    new FormdefPath(path).published
  }

  String getItemSuffix() {
    published? 'form.xhtml' : 'form.xml'
  }

  String display() {
    "${path}: ${title}"
  }

  String toString() {
    "[FormdefVer ${path}: ${title}]"
  }

  /**
   * Bootstrap init causes this method to be used for rendering as XML.
   */
  def toXML(xml) {
    boolean publishedFlag = published
    xml.build {
      ref(id)
      formref(formdef.id)
      created(dateCreated)
      app(appName)
      form(formName)
      path(path)
      verno(published: publishedFlag, fvno)
      publishedFlag? draft() : draft(draft)
      title(title)
      description(description)
      language(language)
    }
  }

  //-------------------- Comparable --------------------

  int hashCode() {
    path.hashCode()
  }

  boolean equals(Object obj) {
    (obj instanceof PxdFormdefVer) && ((PxdFormdefVer)obj).path == path
  }

  // This comparison is different from comparing paths.
  // Highest version and highest draft number comes first.
  int compareTo(Object obj) {
    def other = (PxdFormdefVer)obj
    int outcome = appName.compareTo(other.appName)
    if (outcome == 0) outcome = formName.compareTo(other.formName)
    if (outcome == 0) outcome = -fvno.compareTo(other.fvno)
    if (outcome == 0) outcome = -draft.compareTo(other.draft)
    return outcome
  }

}
