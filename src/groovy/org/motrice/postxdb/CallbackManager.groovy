package org.motrice.postxdb

import java.util.concurrent.Executors

/**
 * Manages callback details, if configured.
 * A callback is an UDP package sent on a database update.
 * ASSUMES the callback destination is on localhost.
 */
class CallbackManager {
  def grailsApplication

  private static final THREAD_POOL_SIZE = 5

  private static final log = org.apache.commons.logging.LogFactory.getLog(this)

  /** The port we send from */
  Integer srcPortNumber = null

  /** The destination port */
  Integer tgtPortNumber = null

  /** Handle to the thread pool */
  def threadPool = null

  def socket = null

  /**
   * Start up the callback manager
   */
  def startup() {
    def srcPortString = grailsApplication.config.postxdb.callback.source.port
    def tgtPortString = grailsApplication.config.postxdb.callback.destination.port
    if (srcPortString && tgtPortString) {
      try {
	srcPortNumber = srcPortString as Integer
	tgtPortNumber = tgtPortString as Integer
      } catch (NumberFormatException exc) {
	srcPortNumber = null
	tgtPortNumber = null
      }
    }

    if (srcPortNumber && tgtPortNumber) {
      socket = new DatagramSocket(srcPortNumber)
      threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
      log.info "Callback manager STARTING on ports ${srcPortNumber}/${tgtPortNumber}"
    } else {
      log.info "Callback manager NOT starting"
    }
  }

  def shutdown() {
    log.info "Callback manager SHUTDOWN"
    threadPool?.shutdown()
    socket?.close()
  }

  /**
   * Callback indicating creation of a draft form definition item.
   */
  def draftFormdefItem(PxdFormdef formdef, PxdFormdefVer formdefVer, PxdItem item) {
    if (threadPool) {
      doCallback('draftFormdefItem', formdef, formdefVer, item, false, false)
    }
  }

  /**
   * Callback indicating creation of a published form definition item.
   */
  def publishedFormdefItem(PxdFormdef formdef, PxdFormdefVer formdefVer, PxdItem item) {
    if (threadPool) {
      doCallback('publishedFormdefItem', formdef, formdefVer, item, false, true)
    }
  }

  /**
   * Callback indicating saving a form instance item.
   */
  def saveInstanceItem(PxdItem item) {
    if (threadPool) {
      doCallback('saveInstanceItem', null, null, item, true, false)
    }
  }

  /**
   * Callback indicating creating an empty form instance item.
   */
  def emptyInstanceItem(PxdItem item) {
    if (threadPool) {
      doCallback('emptyInstanceItem', null, null, item, true, false)
    }
  }

  private doCallback(String op, PxdFormdef formdef, PxdFormdefVer formdefVer, PxdItem item,
		     boolean instance, boolean published)
  {
    def message = "{\"op\":\"${op}\",\"formdefId\":${formdef?.id},\"formdefVerId\":${formdefVer?.id},\"itemId\":${item?.id},\"instance\":${instance},\"published\":${published}}|||"
    def job = new CallbackJob(socket, tgtPortNumber, message)
    if (log.debugEnabled) log.debug "callback << '${message}' on port ${tgtPortNumber}"
    threadPool.submit(job)
  }

}

class CallbackJob implements Runnable {
  def message = null
  def port = null
  def socket = null

  /**
   * Construct a callback job sending a message to the destination.
   * @param socket must be a datagram socket,
   * @param port must be the destination port number,
   * @param message must be the message to send.
   */
  def CallbackJob(DatagramSocket socket, Integer port, String message) {
    this.socket = socket
    this.port = port
    this.message = message
  }

  void run() {
    def bytes = message.getBytes('UTF-8')
    def address = InetAddress.getLocalHost()
    def packet = new DatagramPacket(bytes, bytes.length, address, port)
    socket.send(packet)
  }

}
