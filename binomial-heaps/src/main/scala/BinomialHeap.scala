import leon._
import leon.lang._
import leon.annotation._
import leon.collection._

/*
 * Implementation of Binomial Heap based on "Purely Functionnal Data Structure, Okasaki, P68+"
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */


sealed abstract class BinomialHeap[T] {

	def isEmpty: Boolean = this == BHEmpty[T]()
	def isDefined: Boolean = !this.isEmpty
	//def size: BigInt = {???} ensuring (res => res == this.toList.size && res >= 0)

	def insert(x: T): BinomialHeap[T] = this.insTree(Node(0, x, Nil()))
	def insTree(that: Tree[N]): BinomialHeap[T] = {//t1: that, ts: this
		this match {
			case Cons(t2, rest) => {
				if (that.rank < t2.rank) that :: this
				else rest.insTree(that.link(t2))
			}
			case Nil() => Cons(that, Nil())
		}
	}
	def merge(that: BinomialHeap[T]): BinomialHeap[T] = {
		(this, that) match {
			case (t, Nil()) => t
			case (Nil(), t) => t
			case (t1 :: ts1, t2 :: ts2) => {
				if (t1.rank < t2.rank)  t1 :: ts1.merge(that)
				else if (t2.rank > t1.rank) t2 :: this.merge(ts2)
				else merge(ts1, ts2).insTree(t1.link(t2))
			}
		}
	}
	def findMin(): T = {
		require(this.isDefined)
		this match {
			case BHList[T](Cons(h, Nil())) => h.root()
			case BHList[T](Cons(h, t)) => {
				val x = h.root()
				val y = t.findMin()
				if (TOrdering.lteq(x, y)) x else y
			}
		}
	}
	def deleteMin(): BinomialHeap[T] = {//TODO : see if something is required
		require(this.isDefined)
		this.getMin() match {
			case (TreeNode[T](_, x, ts1), ts2) => ts1.reverse().merge(ts2)
		}
	}
	def getMin(): (Tree, Tree) = {
		require(this.isDefined)
		this match {
			case BHList[T](Cons(t, Nil())) => (h, Nil())
			case BHList[T](Cons(t, ts)) => {
				ts.getMin() match {
					case (tp, tsp) => {
						if (TOrdering.lteq(t.root(), tp.root())) (t, ts)
						else (tp, Cons(t, tsp))
					}
				}
			}
		}
	}
	def reverse(): BinomialHeap[T] = {
		this match {
			case BHEmpty[T]() => this
			case BHList[T](f) => BHList[T](f.reverse)
		}
	}
	
	
	//def toList: List[T] = {???} ensuring (res => this.content == res.content && res.size == this.size && res.size >= 0)
	//def content: Set[T] = {???} ensuring (res => res == this.toList.content /*&& res.size == this.toList.size*/)

}

case class BHList[T](f : List[Tree[T]]) extends BinomialHeap[T]
case class BHEmpty[T]() extends BinomialHeap[T]