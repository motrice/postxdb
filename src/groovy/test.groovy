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