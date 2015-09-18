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

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin

import org.junit.*
import org.motrice.postxdb.XFormsLanguage

@TestMixin(GrailsUnitTestMixin)
class XFormsLanguageTests {
  static final DATADIR = new File('datafortests')

  void testBasics() {
    def formFile = new File(DATADIR, 'submarine-rescue-form.xhtml')
    // Create from existing form
    def converter = new XFormsLanguage(formFile.text)
    def langList = converter.currentLangSet as List
    assert langList.size() == 1
    assert langList[0] == 'en'
    // Try invalid language spec
    shouldFail(IllegalArgumentException) {
      converter.languages('create Swedish')
    }
    // Try language spec where the source language is not in the form
    converter.languages('sv~en.fr')
    assert converter.srcLang == 'sv'
    assert converter.tgtLangList == ['en', 'fr']
    String msg = shouldFail(IllegalArgumentException) {
      converter.validate()
    }
    assert msg.indexOf('POSTXDB.110') >= 0
    // Try valid language spec
    converter.languages('en~sv.fr')
    assert converter.srcLang == 'en'
    assert converter.tgtLangList == ['sv', 'fr']
    // Create a new form version
    def baos = new ByteArrayOutputStream()
    converter.process(baos)
    def xml = new String(baos.toByteArray(), 'UTF-8')

    // Read in the new form version
    def form2 = new XFormsLanguage(xml)
    langList = converter.currentLangSet as List
    assert langList.size() == 3
    // Languages are alphabetically sorted
    assert langList == ['en', 'fr', 'sv']

    // More validation tests

    // Try language spec where the target language already is in the form
    form2.languages('sv~en')
    assert form2.srcLang == 'sv'
    assert form2.tgtLangList == ['en']
    msg = shouldFail(IllegalArgumentException) {
      form2.validate()
    }
    assert msg.indexOf('POSTXDB.116') >= 0

    // Try language spec where the source language is also a target language
    form2.languages('fr~it.en_TW')
    assert form2.srcLang == 'fr'
    assert form2.tgtLangList == ['it', 'en_TW']
    msg = shouldFail(IllegalArgumentException) {
      form2.validate()
    }
    assert msg.indexOf('POSTXDB.112') >= 0
  }

}
