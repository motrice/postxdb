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
/*
 * Script for testing "add language" to an Orbeon form.
 * The class path must include xom.jar
 */

FILE = new File('/home/hs/Downloads/test_t05--v001_08_form.xml')
OUTPUT = new File('/home/hs/Downloads/test_t05--v001_08_output.xml')


def doit(formFile) {
  def form = new org.motrice.postxdb.XFormsLanguage(formFile.text)
  println "Source language: ${form.srcLang}"
  println "Target languages: ${form.tgtLangList}"
  println "Existing languages: ${form.prevLangSet}"
  form.languages()
  println "Source language: ${form.srcLang}"
  println "Target languages: ${form.tgtLangList}"
  println "Existing languages: ${form.prevLangSet}"
  form.validate()
  form.process(new FileOutputStream(OUTPUT))
}

doit(FILE)
