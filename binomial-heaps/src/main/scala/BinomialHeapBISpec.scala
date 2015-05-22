import leon._
import leon.lang._
import leon.annotation._
import leon.collection._
import leon.collection.ListOps._

/*
 * Implementation of Binomial Heap based on "Purely Functionnal Data Structure, Okasaki, P68+"
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */

 // TODO: check if it would be nice to have intermediate parameterized tests.
object BinomialHeapBISpec {

  def testInsert {
    val b1 = BinomialHeapBI.empty.insert(4)
    assert(b1.size == 1)
  }

  def testMin {
    val b1 = BinomialHeapBI.empty.insert(4).insert(2).insert(0).insert(3)
    assert(b1.size == 4)
    assert(b1.findMin == 0)

    val b2 = b1.deleteMin
    assert(b2.size == 3)
    assert(b2.findMin == 2)

    val (e1, b3) = b2.findAndDeleteMin
    assert(e1 == 2)
    assert(b3.size == 2)
    assert(b3.findMin == 3)

    val b4 = b3.deleteMin.deleteMin
    assert(b4.size == 0)
  }

  def testMerge {
    val b1 = BinomialHeapBI.empty.insert(4).insert(2).insert(0).insert(3)
    val b2 = BinomialHeapBI.empty.insert(1).insert(6).insert(5).insert(7)

    val b3 = b1 merge b2
    assert(b3.size == 8)

    assert(b3.findMin == 0)
    assert(b3.deleteMin.findMin == 1)
    assert(b3.deleteMin.deleteMin.findMin == 2)
    assert(b3.deleteMin.deleteMin.deleteMin.findMin == 3)
    assert(b3.deleteMin.deleteMin.deleteMin.deleteMin.findMin == 4)
    assert(b3.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.findMin == 5)
    assert(b3.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.findMin == 6)
    assert(b3.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.deleteMin.findMin == 7)

    val b4 = b3.deleteMin.deleteMin
    val b5 = BinomialHeapBI.empty.insert(8).insert(9).insert(0).insert(10)
    val b6 = BinomialHeapBI.empty.insert(1).insert(12).insert(11).insert(13)
    val b7 = (b6 merge b5) merge b4
    val b8 = (b4 merge b6) merge b5

    assert(b8.findMin == b7.findMin)
    assert(b8.deleteMin.findMin == b7.deleteMin.findMin)
    assert(b8.deleteMin.deleteMin.findMin == b7.deleteMin.deleteMin.findMin)
  }

}