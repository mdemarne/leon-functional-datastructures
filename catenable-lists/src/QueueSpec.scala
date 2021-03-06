import leon._
import leon.lang._
import leon.annotation._
import leon.collection._
import leon.collection.ListOps._

/*
 * Implementation of Queue based on "Purely Functionnal Data Structure, Okasaki, P15+" - test file
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */

object QueueSpec {

  def testSnoc {
      val Q1 = QEmpty[BigInt]()
      val Q2 = Q1.snoc(4)
      assert(Q2.size == 1)
  }	
  
  def testOrder1 {
	  val Q1 = QEmpty[BigInt]()
	  val Q2 = Q1.snoc(1).snoc(2).snoc(3)
	  val Q3 = Q1.snoc(4).snoc(5).snoc(6)
	  val Q4 = Q2 ++ Q3
	  val listQ4: List[BigInt] = Cons(1, Cons(2, Cons(3, Cons(4, Cons(5, Cons(6, Nil()))))))
	  assert(Q4.toList.content == listQ4.content && isSorted(Q4.toList))
  }
  
  def testOrder2 {
	  val Q1 = QCons[BigInt](Cons(1, Cons(2, Cons(3, Nil()))), Cons(6, Cons(5, Cons(4, Nil()))))
	  val Q2 = QCons[BigInt](Cons(7, Cons(8, Cons(9, Nil()))), Cons(12, Cons(11, Cons(10, Nil()))))
	  val Q3 = Q1 ++ Q2
	  val listQ1: List[BigInt] = Cons(1, Cons(2, Cons(3, Cons(4, Cons(5, Cons(6, Nil()))))))
	  val listQ2: List[BigInt] = Cons(7, Cons(8, Cons(9, Cons(10, Cons(11, Cons(12, Nil()))))))
	  assert(Q3.toList.content == listQ1.content ++ listQ2.content && isSorted(Q3.toList)) 
  }
  
  def testTail {
	  val Q1 = QCons[BigInt](Cons(1, Cons(2, Cons(3, Nil()))), Cons(5, Cons(4, Nil())))
	  val listQ1: List[BigInt] = Cons(1, Cons(2, Cons(3, Cons(4, Cons(5, Nil())))))
	  val Q2 = Q1.tail
	  val listQ2: List[BigInt] = Cons(2, Cons(3, Cons(4, Cons(5, Nil()))))
	  assert(Q2.toList.content == listQ2.content && isSorted(Q2.toList))
	  val Q3 = Q2.tail
	  val listQ3: List[BigInt] = Cons(3, Cons(4, Cons(5, Nil())))
	  assert(Q3.toList.content == listQ3.content && isSorted(Q3.toList)) 
	  val Q4 = Q3.tail
	  val listQ4: List[BigInt] = Cons(4, Cons(5, Nil()))
	  assert(Q4.toList.content == listQ4.content && isSorted(Q4.toList)) 
  }

  def testHead {
  	val Q1 = QCons[BigInt](Cons(1, Cons(2, Cons(3, Nil()))), Cons(5, Cons(4, Nil())))
  	assert(Q1.head == 1)
  	assert(Q1.tail.head == 2)
  	assert(Q1.tail.tail.head == 3)
  	assert(Q1.tail.tail.tail.head == 4)
  	assert(Q1.tail.tail.tail.tail.head == 5)
  }

}
