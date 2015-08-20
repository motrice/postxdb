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
