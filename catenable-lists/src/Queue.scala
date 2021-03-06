import leon._
import leon.lang._
import leon.annotation._
import leon.collection._

/*
 * Implementation of Queue based on "Purely Functionnal Data Structure, Okasaki, P15+"
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */

sealed abstract class Queue[T] {

	/* Lower-level API */

	def isEmpty: Boolean = this == QEmpty[T]()

	def isDefined: Boolean = !this.isEmpty

	def size: BigInt = {
		require(this.hasProperShape)
		val res: BigInt = this match {
			case QEmpty() => 0
			case QCons(f, r) => f.size + r.size
		}
		res
	} ensuring (_ >= 0)

	def head: T = {
		require(this.isDefined && this.hasProperShape)
		this match {
			case QCons(f, r) => f.head
		}
	} ensuring (res => this.content.contains(res))

	def tail: Queue[T] = {
		require(this.isDefined && this.hasProperShape)
		val res: Queue[T] = this match {
			case QCons(Cons(e, Nil()), r) if r.isEmpty => QEmpty()
			case QCons(Cons(e, Nil()), r) => QCons(r.reverse, Nil())
			case QCons(Cons(e, es), r) => QCons(es, r)
		}
		res
	} ensuring (res => res.hasProperShape && res.size + 1 == this.size)

	def snoc(x: T): Queue[T] = {
		require(this.hasProperShape)
		this match {
			case QEmpty() =>  QCons(x :: Nil(), Nil())
			case QCons(f, r) => QCons(f, x :: r)
		}
	} ensuring (res => res.isDefined && res.hasProperShape && res.size - 1 == this.size)

	/* This is not in the specification, but we implemented it anyway for testing purposes */
	def ++(that: Queue[T]): Queue[T] = {
		require(this.hasProperShape && that.hasProperShape)
		(this, that) match {
			case (QEmpty(), _) => that
			case (_, QEmpty()) => this
			// TODO: might be worth doing the ++ in a lazy manner only, if possible. ++ takes time compared
			// to other accesses in O(1) (snoc, head, tail, etc). Another possibility would be to implement
			// those queues using QCons(List[T], List[List[T]]) for instance! But ++ might very well not be
			// required.
			case (QCons(f1, r1), QCons(f2, r2)) => QCons(f1, r2 ++ f2.reverse ++ r1)
		}
	} ensuring(res => res.hasProperShape && res.size == this.size + that.size && res.content == this.content ++ that.content)

	/* Structure transformation */

	def toList: List[T] = {
		require(this.hasProperShape)
		val res: List[T] = this match {
			case QEmpty() => Nil()
			case QCons(f, r) => f ++ r.reverse
		}
	 	res
	 } ensuring (res => this.content == res.content && res.size == this.size && res.size >= 0)


	def content: Set[T] = {
		require(this.hasProperShape)
		this match {
			case QEmpty() => Set()
			case QCons(f, r) => f.content ++ r.content
		}
	}

	/* Higher-order API */

	def map[R](func: T => R): Queue[R] = {
		require(this.hasProperShape)
		val res: Queue[R] = this match {
			case QEmpty() => QEmpty()
			case QCons(f, r) => QCons(f.map(func(_)), r.map(func(_)))
		}
		res
	} ensuring (_.size == this.size)

	// TODO: to use flatMap, we need to ensure that the queue returned by func has a proper shape. 
	// Unfortunately, there is no way for now to do that properly using Leon.
	/*def flatMap[R](func: T => Queue[R]): Queue[R] =  {
		require(this.hasProperShape)
		val res: Queue[R] = this match {
			case QEmpty() => QEmpty()
			case q => func(q.head) ++ q.tail.flatMap(func)
		}
		res
	} ensuring (res => res.hasProperShape)*/

	def forall(func: T => Boolean): Boolean = this match {
		case QEmpty() => true /* Default, as in Scala standards */
		case QCons(f, r) => f.forall(func(_)) && r.forall(func(_))
	}

	def exists(func: T => Boolean): Boolean = this match {
		case QEmpty() => false
		case QCons(f, r) => f.exists(func(_)) || r.exists(func(_))
	}

	def foldLeft[R](z: R)(func: (R, T) => R): R = {
		require(this.hasProperShape)
		this match {
			case QEmpty() => z
			case q => q.tail.foldLeft(func(z,q.head))(func)
		}
	}

	/* Invariants */

	def hasProperShape = this match {
		case QEmpty() => true
		case QCons(f, r) => !f.isEmpty
	}
}

/* Companion object */
object Queue {

	def empty[T] = QEmpty[T]()
	// Ideally, this should be done using repeated, but it is not implemented in leon.
	def apply[T](e: T) = empty.snoc(e)

}

case class QCons[T](f : List[T], r: List[T]) extends Queue[T]
case class QEmpty[T]() extends Queue[T]
