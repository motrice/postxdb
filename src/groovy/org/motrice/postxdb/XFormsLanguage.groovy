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

import java.util.regex.*
import nu.xom.*

/**
 * Add languages to an XForms form definition.
 * Tested only with Orbeon Forms.
 * Usage: Construct, optional languages(), validate(), process().
 */
class XFormsLanguage {
  // Namespaces
  static final String XF_PF = 'xf'
  static final String XF_NS = 'http://www.w3.org/2002/xforms'
  static final String XH_PF = 'xh'
  static final String XH_NS = 'http://www.w3.org/1999/xhtml'
  static final String XML_PF = 'xml'
  static final String XML_NS = 'http://www.w3.org/XML/1998/namespace'

  static final Pattern LANG = ~/\s*\[\[lang:\s*([a-zA-Z_]+)~([a-zA-Z_.]+)\]\](.*)/
  static final Pattern SPEC = ~/([a-zA-Z_]+)~([a-zA-Z_.]+)/

  /**
   * Form XML as parsed by a Builder
   */
  final doc

  /**
   * Source language found in the title, or null.
   */
  String srcLang

  /**
   * Target languages found in the title, or null. List of String.
   */
  List tgtLangList

  /**
   * Language codes in the form.
   * Set by the constructor and updated when adding languages.
   * SortedSet of String.
   */
  SortedSet currentLangSet

  /**
   * Construct from form XML.
   * Throws exception if there is a parsing problem.
   * Previous languages are always collected.
   */
  def XFormsLanguage(String xmlText) {
    def xc = initNamespaces()
    def builder = new Builder()
    doc = builder.build(new StringReader(xmlText))
    def langSet = new TreeSet()
    doc.query('//@xml:lang').each {node -> langSet.add(node.value)}
    this.currentLangSet = langSet
  }

  /**
   * Collect language information from titles.
   * There is no way to know which title is used if there is more than one
   * language specification.
   */
  def languages() {
    def titles = doc.query('//*[@id="fr-form-metadata"][1]/metadata/title')
    titles.each {title ->
      title.each {node ->
	if (node instanceof Text) processTitle(node, true)
      }
    }
  }

  /**
   * Define a language spec for processing.
   * This method overrides any and all languages set by the constructor.
   */
  def languages(String langSpec) {
    def m = SPEC.matcher(langSpec.trim())
    if (m.matches()) {
      this.srcLang = m.group(1)
      this.tgtLangList = m.group(2).split(/\./)
    } else {
      String msg = "Language spec [${langSpec}] syntax error"
      throw new IllegalArgumentException("POSTXDB.113|${msg}")
    }
  }

  /**
   * Add languages according to the language spec.
   * Write the resulting form to an output stream.
   * Add the new languages to currentLangSet.
   */
  def process(OutputStream out) {
    // Find all instances of the source language
    doc.query('//@xml:lang').each {attr ->
      if (attr.value == srcLang) doProcess(attr.parent)
    }

    // Write the output
    def serializer = new Serializer(out, 'UTF-8')
    serializer.write(doc)
  }

  /**
   * Copy a node that has a language attribute equal to the
   * source language.
   */
  private doProcess(Node srcNode) {
    def parent = srcNode.parent
    def srcIdx = parent.indexOf(srcNode)
    // If this is a title, remove any language control stuff
    if ((srcNode instanceof Element) && srcNode.localName == 'title') {
      srcNode.each {node ->
	if (node instanceof Text) processTitle(node, false)
      }
    }
    // Add a copy for each target language
    tgtLangList.each {tgtLang ->
      def tgtNode = srcNode.copy()
      def langAttr = tgtNode.getAttribute('lang', XML_NS)
      langAttr.value = tgtLang
      // Use the index to insert copies consecutively
      parent.insertChild(tgtNode, ++srcIdx)
      currentLangSet.add(tgtLang)
    }
  }

  /**
   * Validate input before processing.
   * NOTE: The form title may have been modified by the constructor.
   * Returns true if validation passes, otherwise throws an exception.
   */
  def validate() {
    if (!currentLangSet.contains(srcLang)) {
      String msg = "The source language [${srcLang}] is not present in the form"
      throw new IllegalArgumentException("POSTXDB.110|${msg}")
    }

    def alreadyThere = tgtLangList.find {tgtLang ->
      currentLangSet.contains(tgtLang)
    }

    if (alreadyThere) {
      String msg = "Target language [${alreadyThere}] is already present in the form"
      throw new IllegalArgumentException("POSTXDB.116|${msg}")
    }

    if (tgtLangList.contains(srcLang)) {
      String msg = "Language [${srcLang}] cannot be both source and target language"
      throw new IllegalArgumentException("POSTXDB.111|${msg}")
    }

    def languages = new HashSet()
    languages.addAll(Locale.ISOLanguages)
    def locales = new HashSet()
    locales.addAll(Locale.availableLocales*.toString())
    tgtLangList.each {lang ->
      if (!locales.contains(lang) && !languages.contains(lang)) {
	String msg = "Target language [${lang}] is not a known language or locale"
	throw new IllegalArgumentException("POSTXDB.112|${msg}")
      }
    }
  }

  String toString() {
    "[XFormsLanguage prev:${currentLangSet} src:${srcLang} tgt:${tgtLangList}]"
  }

  //------------- Private methods -------------

  /**
   * Create a namespace context for XPath uses in this application.
   */
  private static XPathContext initNamespaces() {
    def xc = new XPathContext()
    xc.addNamespace(XF_PF, XF_NS)
    xc.addNamespace(XH_PF, XH_NS)
    //xc.addNamespace('xh', '')
    return xc
  }

  /**
   * Process the title of the form.
   * Check if there is a language spec and extract its parts.
   * Remove the language spec from the title.
   */
  private processTitle(Text textNode, boolean changeLanguages) {
    def m = LANG.matcher(textNode.value)
    if (m.matches()) {
      if (changeLanguages) {
	this.srcLang = m.group(1)
	this.tgtLangList = m.group(2).split(/\./)
      }
      textNode.value = m.group(3)
    }
  }

  //--- Read-only setters

  private setCurrentLangSet(SortedSet value) {
    currentLangSet = value
  }

  private setSrcLang(String value) {
    srcLang = value
  }

  private setTgtLangList(List value) {
    tgtLangList = value
  }

  //------------- Static initializer -------------

  /**
   * Add Grooviness to XOM.
   */
  static {XomGrooviness.unleash()}

}
