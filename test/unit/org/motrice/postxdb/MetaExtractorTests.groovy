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
import org.motrice.postxdb.MetaExtractor;

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 * XML files used by the tests are located in the main project directory
 */
@TestMixin(GrailsUnitTestMixin)
class MetaExtractorTests {
  static final DATADIR = new File('datafortests')

  void testBasics() {
    def formFile = new File(DATADIR, 'submarine-rescue-form.xhtml')
    def meta = MetaExtractor.extract(formFile.text)
    assert meta
    assert meta.app == 'submarine'
    assert meta.form == 'rescue'
    assert meta.title.trim() == 'Rescue Application'
    assert meta.description == 'Fill in this form for submarine rescue action. Application is promptly processed the next day. In most cases. Obviously not during bank holidays.'
    assert meta.author == null
    assert meta.language == 'en'
    assert meta.logo == '/fr/service/exist/crud/orbeon/builder/data/277f56f04a9dc32b3cc316843a5414fb/75b304166dc3354af84f8e6bad327f6f.png'
  }

}
