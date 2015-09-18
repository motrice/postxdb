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

import nu.xom.*

/**
 * Enhance XOM with Groovy-style goodies.
 * Usage: Include a static initializer in your code, invoking unleash().
 */
class XomGrooviness {
  /**
   * Do not construct.
   */
  private XomGrooviness() {}

  static unleash() {
    // With iterator the usual "each" etc can be used on Nodes
    nu.xom.Nodes.metaClass.iterator << {-> new NodeListIterator(delegate)}
    // nodeList[int]
    nu.xom.Nodes.metaClass.getAt << {Integer idx -> delegate.get(idx)}
    // Groovy truth

    // node[int] gets child with index int
    nu.xom.Node.metaClass.getAt << {Integer idx -> delegate.getChild(idx)}
    // Iterate over child nodes.
    nu.xom.Node.metaClass.iterator << {-> new NodeIterator(delegate)}
    // Concatenate the values of all text node children.
    nu.xom.Node.metaClass.getText << {->
      def iter = new NodeIterator(delegate)
      def result = iter.inject("") {text, node ->
	return (node instanceof Text)? text + node.value : text
      }

      return result
    }
    // Create a Map containing all attributes of a node.
    // Local names are keys.
    nu.xom.Node.metaClass.getAttributes << {->
      def iter = new AttrIterator(delegate)
      def result = [:]
      result = iter.inject(result) {map, attr ->
	map[attr.localName] = attr.value
	return map
      }

      return result
    }
  }

}

/**
 * A read-only node list iterator for XOM.
 */
class NodeListIterator implements Iterator { 

  private final Nodes nodeList
  private int idx

  def NodeListIterator(Nodes nodeList) {
    this.nodeList = nodeList
    idx = 0
  }
  
  boolean hasNext() {
    idx <= maxIdx
  }

  def next() {
    if (!hasNext()) throw new NoSuchElementException()
    nodeList.get(idx++)
  }

  void remove() {
    throw new UnsupportedOperationException()
  }

  private getMaxIdx() {
    nodeList.size() - 1
  }

}

/**
 * A read-only node iterator for XOM, iterating over child nodes.
 * Also works for ParentNode and Element.
 */
class NodeIterator implements Iterator { 

  private final parent
  private int idx

  def NodeIterator(parent) {
    this.parent = parent
    idx = 0
  }
  
  boolean hasNext() {
    idx <= maxIdx
  }

  def next() {
    if (!hasNext()) throw new NoSuchElementException()
    parent.getChild(idx++)
  }

  void remove() {
    throw new UnsupportedOperationException()
  }

  private getMaxIdx() {
    parent.childCount - 1
  }

}

/**
 * A read-only attribute iterator for XOM.
 */
class AttrIterator implements Iterator { 

  private final parent
  private int idx

  def AttrIterator(parent) {
    this.parent = parent
    idx = 0
  }
  
  boolean hasNext() {
    idx <= maxIdx
  }

  def next() {
    if (!hasNext()) throw new NoSuchElementException()
    parent.getAttribute(idx++)
  }

  void remove() {
    throw new UnsupportedOperationException()
  }

  private getMaxIdx() {
    parent.attributeCount - 1
  }

}
