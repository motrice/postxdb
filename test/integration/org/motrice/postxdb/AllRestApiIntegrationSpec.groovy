package org.motrice.postxdb

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll
import grails.test.spock.IntegrationSpec
// To access the config (both getting and setting) in tests
import static grails.util.Holders.config as grailsConfig

/**
 * Test the sequence of storing a form definition in an empty database,
 * then creating and storing form data based on the form definition.
 * The tests simulate the way Orbeon would call Postxdb, but this is
 * obviously not an end-to-end test.
 */
@Stepwise
class AllRestApiIntegrationSpec extends IntegrationSpec {
  static final DATADIR = new File('datafortests')
  static final PHOTO_RESOURCE = 'c4fefeea7ee0b4747b7b5ef849cdbcaca981995c.bin'
  static final DRAFT_UUID = '999ffef3cbe94bf220d21ff82d217b454f19da2b'
  static final UNVERSIONED_FORMDEF = 'postxdb_test02--unversioned_form.xml'
  static final FORM_DATA01_UUID = '84f600c2dfadee38f753bbb61182dfd0b96ade7a'
  static final String FORM_DATA01_PATH = "${FORM_DATA01_UUID}_data.xml"
  static final FORM_DATA02_UUID = '8aebb81fafd6c4fe844c69b93e786c1db7d45f0c'
  static final String FORM_DATA02_PATH = "${FORM_DATA02_UUID}_data.xml"
  static final FORM_DATA02_IMAGE = '000c3e61c6746c736403dd694c8c5d805e0d0439.bin'
  static final FORM_DATA03_UUID = '2b38370625bffb24425c2571ca099c0d40eb8acf'
  static final APP_NAME = 'postxdb'
  static final PUBLISHED_FORM_NAME = 'test02--v001'

  @Shared photoSize = 0
  @Shared draftSize = 0
  @Shared publishedSize = 0
  @Shared customHeader = grailsConfig.postxdb.itempath.header
  @Shared formdefId = ''
  @Shared publishedVerId = ''
  @Shared itemList
  @Shared formDataUuid = ''

  //=========== FORM DEFINITION TESTS ===========

  def 'Store a form definition resource: A photo'() {
  given: 'The photo resides as a file in DATADIR'
    def rrc = new RestResourceController()

  and: 'set request parameters'
    rrc.params.uuid = DRAFT_UUID
    rrc.params.resource = PHOTO_RESOURCE
    def photo = new File(DATADIR, PHOTO_RESOURCE).bytes
    rrc.request.content = photo
    photoSize = photo.size()

  when: 'call putop'
    rrc.putop()

  then: 'check outcome'
    rrc.response.status == 201
    rrc.response.getHeader(customHeader) == PHOTO_RESOURCE
  }

  def 'Check that we can retrieve the photo stored in the previous test'() {
  given: 'We have just stored a photo as a resource'
    def rrc = new RestResourceController()

  and: 'set request parameters'
    // Arbitrary uuid
    rrc.params.uuid = 'aabb'
    rrc.params.resource = PHOTO_RESOURCE

  when: 'call getop'
    rrc.getop()

  then: 'check that we got the photo'
    rrc.response.status == 200
    // See class org.springframework.mock.web.MockHttpServletResponse
    rrc.response.contentAsByteArray?.size() == photoSize
  }

  /**
   * When Orbeon first stores a new form definition draft there is no version,
   * just app and form names.
   */
  def 'Store a draft form definition that refers to the above photo'() {
  given: 'An unversioned draft form exists as a file in DATADIR'
    def rrc = new RestResourceController()

  and: 'set request parameters'
    rrc.params.uuid = DRAFT_UUID
    rrc.params.resource = 'data.xml'
    def text = new File(DATADIR, UNVERSIONED_FORMDEF).text
    draftSize = text?.size()
    rrc.request.xml = text

  when: 'call putop'
    rrc.response.reset()
    rrc.putop()

  then: 'check outcome'
    rrc.response.status == 201
    rrc.response.getHeader(customHeader) == 'postxdb/test02--v001_01/form.xml'
  }

  /**
   * Note that we don't get back exactly what we stored because
   * a version has been added.
   */
  def 'Check that we can retrive the draft form definition'() {
  given: 'We have just stored a draft form definition'
    def rrc = new RestResourceController()

  and: 'set request parameters'
    rrc.params.uuid = DRAFT_UUID
    rrc.params.resource = 'data.xml'

  when: 'call getop'
    // Reset necessary. It seems responses are accumulated.
    rrc.response.reset()
    rrc.getop()

  then: 'check that we got the form definition'
    rrc.response.status == 200
    def text = rrc.response.text
    // What we retrieve should be roughly the original size :-)
    Math.abs(text.size() - draftSize) < 12
  }

  def 'Check that we now have exactly one form definition'() {
  given: 'We have just stored the first form definition draft'
    def rpc = new RestPostxdbController()

  when: 'call formdefget (no parameters)'
    rpc.response.reset()
    rpc.formdefget()

  then: 'check the outcome'
    rpc.response.status == 200
    checkSingleFormdef(rpc.response.text, 'postxdb/test02--v001_01')
  }

  def 'Check that we have exactly one form definition version'() {
  given: 'We have just stored the first form definition draft'
    def rpc = new RestPostxdbController()

  and: 'set request params'
    // Has to be actively reset. It seems params stick.
    rpc.params.uuid = null
    // Note: String expected
    rpc.params.formdef = formdefId

  when: 'call versionget'
    rpc.response.reset()
    rpc.versionget(null)

  then: 'check the outcome'
    rpc.response.status == 200
    checkSingleFormdefVer(rpc.response.text, 'postxdb/test02--v001_01')
  }

  def 'Save the first draft as a new draft'() {
  given: 'The text of the first draft was saved in a shared variable above'
    def rrc = new RestResourceController()
    def prevText = readItemText('postxdb/test02--v001_01/form.xml')
    def formText = prevText?.replaceAll('Åland', 'Öland')

  and: 'set request parameters'
    rrc.response.reset()
    rrc.params.uuid = DRAFT_UUID
    rrc.params.resource = 'data.xml'
    rrc.request.xml = formText

  when: 'call putop'
    rrc.putop()

  then: 'check outcome'
    rrc.response.status == 201
    rrc.response.getHeader(customHeader) == 'postxdb/test02--v001_02/form.xml'
  }

  def 'Check that we have two form definition versions'() {
  given: 'We have just saved the updated form definition draft'
    def rpc = new RestPostxdbController()

  and: 'set request params'
    // Has to be actively reset. It seems params stick.
    rpc.params.uuid = null
    rpc.params.resource = null
    // Note: String expected
    rpc.params.formdef = formdefId

  when: 'call versionget'
    rpc.response.reset()
    rpc.versionget(null)

  then: 'check the outcome'
    rpc.response.status == 200
    def text = rpc.response.text
    checkLatestFormdefVer(text, 2, 'postxdb/test02--v001_02')
  }

  def 'Retrive the second form definition draft'() {
  given: 'We have just stored a second draft'
    def rrc = new RestResourceController()

  and: 'set request parameters'
    rrc.params.uuid = DRAFT_UUID
    rrc.params.resource = 'data.xml'

  when: 'call getop'
    // Reset necessary. It seems responses are accumulated.
    rrc.response.reset()
    rrc.getop()

  then: 'check that we got the form definition'
    rrc.response.status == 200
    def text = rrc.response.text
  }

  def 'First step of publishing the form def: Store resource (like Orbeon does)'() {
  given: 'Begin by storing the photo even though it is a no-op in Postxdb'
    // Note: A different controller now
    def rfc = new RestFormdefController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.resource = PHOTO_RESOURCE
    def photo = new File(DATADIR, PHOTO_RESOURCE).bytes
    // Request: org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
    rfc.request.format = 'binary'
    rfc.request.content = photo

  when: 'call putop'
    rfc.response.reset()
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == PHOTO_RESOURCE
  }

  def 'Second step of publishing the form def: Store the XML from the latest draft'() {
  given: 'We retrieved the second draft above'
    def rfc = new RestFormdefController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.resource = 'form.xhtml'
    def text = readItemText('postxdb/test02--v001_02/form.xml')
    publishedSize = text?.size()
    rfc.request.xml = text

  when: 'call putop'
    rfc.response.reset()
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == 'postxdb/test02--v001/form.xhtml'
  }

  def 'Check that we can retrieve the published form definition'() {
  given: 'We have just published a form definition'
    def rfc = new RestFormdefController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.resource = 'form.xhtml'

  when: 'call getop'
    rfc.response.reset()
    rfc.getop()

  then: 'check outcome'
    rfc.response.status == 200
    // Metadata is updated, so the sizes are somewhat different
    Math.abs(rfc.response.text.size()- publishedSize) < 12
  }

  def 'Check that we have four form definition versions'() {
  given: 'We have just saved the published form definition'
    def rpc = new RestPostxdbController()

  and: 'set request params'
    // Has to be actively reset. It seems params stick.
    rpc.params.uuid = null
    rpc.params.resource = null
    // Note: String expected
    rpc.params.formdef = formdefId

  when: 'call versionget'
    rpc.response.reset()
    rpc.versionget(null)

  then: 'check the outcome'
    rpc.response.status == 200
    def text = rpc.response.text
    checkLatestFormdefVer(text, 4, 'postxdb/test02--v002_01')
    savePublishedFormdefVerId(text)
  }

  def 'Get all definition items at this point'() {
  given: 'We have created a form definition with 2 drafts and 1 published version'
    def rpc = new RestPostxdbController()

  and: 'set request parameters'
    rpc.params.formdef = null
    rpc.params.uuid = DRAFT_UUID

  when: 'call defitemget'
    rpc.response.reset()
    rpc.defitemget()
    itemList = convertDefitemOutput(rpc.response.text)

  then: 'check outcome'
    rpc.response.status == 200
    itemList.size() == 5
  }

  @Unroll
  def 'Check that item #path has format #format '(String path, String format) {
  given: 'a list of item maps from the previous test'

  when: 'search for a path'
    def map = itemList.find { it.path == path }

  then: 'search the item map for the given id'
    map != null
    map.format == format

  where:
  path | format
  'c4fefeea7ee0b4747b7b5ef849cdbcaca981995c.bin' | 'binary'
  'postxdb/test02--v001_01/form.xml' | 'xml'
  'postxdb/test02--v001_02/form.xml' | 'xml'
  'postxdb/test02--v001/form.xhtml' | 'xml'
  'postxdb/test02--v002_01/form.xml' | 'xml'
  }

  def 'Check that there are no definition items yet'() {
  given: 'A previous test found the id of the published form def version'
    def rpc = new RestPostxdbController()

  and: 'set request parameters'
    rpc.params.formdefver = publishedVerId
    rpc.params.formdef = null
    rpc.params.uuid = null

  when: 'call institemget'
    rpc.response.reset()
    rpc.institemget()
    def text = rpc.response.text

  then: 'check outcome'
    rpc.response.status == 404
  }

  //=========== FORM DATA TESTS ===========

  def 'Create and store a form instance. No attached image'() {
  given: 'A form instance in DATADIR'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = FORM_DATA01_UUID
    rfc.params.resource = 'data.xml'

  when: 'call putop'
    rfc.response.reset()
    rfc.request.xml = new File(DATADIR, FORM_DATA01_PATH).text
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == '84f600c2dfadee38f753bbb61182dfd0b96ade7a/data.xml'
  }

  def 'Create an empty new item, pick up the uuid'() {
  given: 'Nothing special'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = null
    rfc.params.resource = null

  when: 'call newop'
    rfc.response.reset()
    rfc.newop()
    formDataUuid = rfc.response.text

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == "${formDataUuid}/data.xml"
  }

  def 'Store an image attachment for a later form instance'() {
  given: 'The uuid created in the previous test'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = formDataUuid
    rfc.params.resource = FORM_DATA02_IMAGE
    def photo = new File(DATADIR, FORM_DATA02_IMAGE).bytes
    // Second usage of this variable
    photoSize = photo.size()
    rfc.request.content = photo
    rfc.request.format = 'binary'

  when: 'call putop'
    rfc.response.reset()
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == FORM_DATA02_IMAGE
  }

  /**
   * Note that a different controller is tested as compared to the form definition
   * photo in one of the first test cases.
   */
  def 'Check that we can retrieve the attachment stored in the previous test'() {
  given: 'We have just stored an image'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = formDataUuid
    rfc.params.resource = FORM_DATA02_IMAGE

  when: 'call getop'
    rfc.response.reset()
    rfc.getop()

  then: 'check that we got the photo'
    rfc.response.status == 200
    // See class org.springframework.mock.web.MockHttpServletResponse
    rfc.response.contentAsByteArray?.size() == photoSize
  }

  def 'Create and store a form instance that attaches the previous image'() {
  given: 'Form data XML is stored in DATADIR, the uuid is picked up from the previous test'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = formDataUuid
    rfc.params.resource = 'data.xml'

  when: 'call putop'
    rfc.response.reset()
    rfc.request.xml = new File(DATADIR, FORM_DATA02_PATH).text
    rfc.request.format = 'xml'
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 201
    rfc.response.getHeader(customHeader) == "${formDataUuid}/data.xml"
  }

  def 'Closer check of form instance 1 stored in previous test'() {
  given: 'Two form instances have just been stored'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = FORM_DATA01_UUID
    rfc.params.resource = 'data.xml'

  when: 'call getop'
    rfc.response.reset()
    rfc.getop()
    def xml = rfc.response.text

  then: 'check that we got the photo'
    rfc.response.status == 200
    // Contains a reference to the photo included in the form definition
    xml.indexOf(PHOTO_RESOURCE) >= 0
    // Does not contain a reference to the attached image
    xml.indexOf(FORM_DATA02_IMAGE) < 0
  }

  def 'Closer check of form instance 2 stored in previous test'() {
  given: 'Two form instances have just been stored'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = formDataUuid
    rfc.params.resource = 'data.xml'

  when: 'call getop'
    rfc.response.reset()
    rfc.getop()
    def xml = rfc.response.text

  then: 'check that we got the photo'
    rfc.response.status == 200
    // Contains a reference to the photo included in the form definition
    xml.indexOf(PHOTO_RESOURCE) >= 0
    // Does not contain a reference to the attached image
    xml.indexOf(FORM_DATA02_IMAGE) >= 0
  }

  def 'Duplicate instance 2 stored above. Make the original read-only.'() {
  given: 'A previous test found the id of the published form def version'
    def rpc = new RestPostxdbController()

  and: 'set request parameters'
    rpc.params.app = null
    rpc.params.form = null
    rpc.params.uuid = null
    rpc.params.resource = null
    rpc.params.srcuuid = formDataUuid
    rpc.params.tgtuuid = FORM_DATA03_UUID

  when: 'call duplicateinstance'
    rpc.response.reset()
    rpc.duplicateinstance(null)
    def xml = rpc.response.text
    def map = convertDuplicateitemOutput(xml)

  then: 'check the response'
    rpc.response.status == 200
    map.uuid == FORM_DATA03_UUID
    map.formpath == "${APP_NAME}/${PUBLISHED_FORM_NAME}"
    // Check that the duplicate points back to the original
    readItemUuid(map.origin) == formDataUuid
  }

  def 'Check that we cannot write to the instance duplicated in the previous test'() {
  given: 'Reuse the original data, it should not matter'
    def rfc = new RestFormdataController()

  and: 'set request parameters'
    rfc.params.app = APP_NAME
    rfc.params.form = PUBLISHED_FORM_NAME
    rfc.params.uuid = formDataUuid
    rfc.params.resource = 'data.xml'

  when: 'call putop'
    rfc.response.reset()
    rfc.request.xml = new File(DATADIR, FORM_DATA02_PATH).text
    rfc.request.format = 'xml'
    rfc.putop()

  then: 'check outcome'
    rfc.response.status == 403
  }

  //======= HELPER METHODS =======

  /**
   * Check that the output of a formdefget call contains a single formdef
   * and that its current draft is the expected one.
   * SIDE EFFECT: Store the id of the formdef in formdefId.
   */
  void checkSingleFormdef(String xml, String expectedCurrentDraft) {
    def list = convertFormdefOutput(xml)
    assert list.size() == 1
    def formdef = list[0]
    assert formdef.currentDraft == expectedCurrentDraft
    formdefId = formdef.ref
  }

  void checkSingleFormdefVer(String xml, String expectedPath) {
    def list = convertFormdefVerOutput(xml)
    assert list.size() == 1
    def formdefVer = list[0]
    assert formdefVer.path == expectedPath
  }

  void checkLatestFormdefVer(String xml, Integer expectedSize, String expectedPath) {
    def list = convertFormdefVerOutput(xml)
    assert list.size() == expectedSize
    def formdefVer = list[0]
    assert formdefVer.path == expectedPath
  }

  /**
   * From a list of form definitions, find the first published one
   * and save its id.
   * The input must be the output from the formdefver REST API.
   */
  void savePublishedFormdefVerId(String xml) {
    def list = convertFormdefVerOutput(xml)
    def map = list.find { it.published == 'true' }
    assert map != null
    publishedVerId = map.ref
  }

  //======= PRIVATE METHODS =======

  /**
   * Convert the output of the formdef REST API to List of Map.
   */
  private List convertFormdefOutput(String xml) {
    def result = []
    def list = new XmlSlurper().parseText(xml)
    list.pxdFormdef.each {formdef ->
      def entry = [:]
      formdef.'*'.each {node ->
	entry[node.name()] = node.text()
      }

      result.add(entry)
    }

    return result
  }

  /**
   * Convert the output of the formdefver REST API to List of Map.
   * The output is sorted by version, beginning with the latest one.
   */
  private List convertFormdefVerOutput(String xml) {
    def result = []
    def list = new XmlSlurper().parseText(xml)
    list.pxdFormdefVer.each {formdefVer ->
      def entry = [:]
      formdefVer.'*'.each {node ->
	def name = node.name()
	entry[name] = node.text()
	if (name == 'verno') entry.published = node.@published.text()
      }

      result.add(entry)
    }

    return result
  }

  /**
   * Convert the output of the defitem REST API to List of Map.
   */
  private List convertDefitemOutput(String xml) {
    def result = []
    def list = new XmlSlurper().parseText(xml)
    list.pxdItem.each {item ->
      def entry = [:]
      item.'*'.each {node ->
	entry[node.name()] = node.text()
      }

      result.add(entry)
    }

    return result
  }

  /**
   * Convert the output of the duplicateitem REST API to Map.
   */
  private Map convertDuplicateitemOutput(String xml) {
    def result = []
    def list = new XmlSlurper().parseText(xml)
    list.pxdItem.each {item ->
      def entry = [:]
      item.'*'.each {node ->
	entry[node.name()] = node.text()
      }

      result.add(entry)
    }

    assert result.size() == 1
    return result[0]
  }

  /**
   * Return the text of an item specified by a path.
   * There is an encoding problem when using the REST API to get item text.
   * It seems to be inherent in the test environment, not in actual use.
   */
  private String readItemText(String itemPath) {
    def item = PxdItem.findByPath(itemPath)
    return item? item.text : null
  }

  /**
   * Retrieve an item by id, return its uuid.
   */
  private String readItemUuid(id) {
    def item = PxdItem.get(id)
    return item?.uuid
  }

}
