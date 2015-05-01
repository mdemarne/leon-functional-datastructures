import leon._
import leon.lang._
import leon.annotation._
import leon.collection._

/*
 * Implementation of Catenable List based on "Purely Functionnal Data Structure, Okasaki, P93+"
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */

 // DONE: 1) verify and finish all structures
 // DONE: 2) add better checks and add external func (content, toList, etc.)!
 // 						SOLVED 1. Some problems (weird errors from Leon side) using content.
 //							DONE 2. Adding some tests based on forall instead of content.
 // DONE: add more Spec tests.

 // DONE: comparisons of contents of sets in ensuring makes java exception
 //		=> problem came from the use of flatMaps. Using recursive functions instead.

sealed abstract class CatenableList[T] {

	/* Lower-level API */

	def isEmpty: Boolean = this == CEmpty[T]()

	def isDefined: Boolean = !this.isEmpty

	def size: BigInt = {
		require(this.hasProperShape)
		this match {
			case CEmpty() => 0
			case CCons(h, t) => 1 + CatenableList.sumTail(t)
		}
	} ensuring (_ >= 0)

	def cons(x: T): CatenableList[T] = {
		require(this.hasProperShape)
		CCons(x, QEmpty[CatenableList[T]]()) ++ this
	} ensuring(res => res.content == this.content ++ Set(x) && res.head == x && res.size == this.size + 1)

	def snoc(x: T): CatenableList[T] = {
		require(this.hasProperShape)
		this ++ CCons(x, QEmpty[CatenableList[T]]())
	} ensuring(res => res.content == this.content ++ Set(x) && res.size == this.size + 1)

	def ++(that: CatenableList[T]): CatenableList[T] = {
		require(this.hasProperShape && that.hasProperShape)
		(this, that) match {
			case (CEmpty(), _) => that
			case (_, CEmpty()) => this
			case _ => this.link(that)
		}
	} ensuring(res => res.content == this.content ++ that.content && res.size == this.size + that.size)

	def head: T = {
		require(this.isDefined && this.hasProperShape)
		this match {
			case CCons(h, t) => h
		}
	} ensuring(res => this.contains(res))


	def tail: CatenableList[T] = {
		require(this.isDefined && this.hasProperShape)
		this match {
			case CCons(h, t) if t.isEmpty => CEmpty()
			case CCons(h, t) => CatenableList.linkAll(t)
		}
	} ensuring(res => (this.forall(res.contains(_)) || res == CEmpty[T]()) && res.size == this.size - 1)

	/* Structure transformation */

	def content: Set[T] = this match {
		case CEmpty() => Set()
		case CCons(h, t) =>
			// TODO: remove the val once inlinine issue resolved
			// Set(h) ++ (t.toList.flatMap(_.toList)).content
			val st1 = CatenableList.queueOfCatToContent(t)
			Set(h) ++ st1
	}

	def toList: List[T] = (this match {
		case CEmpty() => Nil()
		case CCons(h, t) => 
			val st1 = CatenableList.queueOfCatToList(t)
			h :: st1
	}) ensuring(res => res.content == this.content)

	/* high-level API */

	def forall(func: T => Boolean): Boolean = this match {
		case CEmpty() => true
		case CCons(h, t) => func(h) && t.forall(_.forall(func))
	}

	def contains(x: T): Boolean = this match {
		case CEmpty() => false
		case CCons(h, t) if h == x => true
		case CCons(h, t) => t.exists(_.contains(x))
	}

	/* Helpers */

	private def link(that: CatenableList[T]): CatenableList[T] = {
		require(this.isDefined && this.hasProperShape && that.isDefined && that.hasProperShape)
		this match {
			case CCons(h, t) => CCons(h, t.snoc(that)) // TODO : p96 : "tree suspension"
		}
	} ensuring(res => /*res.content == this.content ++ that.content &&*/ res.size == this.size + that.size) // TODO: more ? on structure

	/* Invariants */

	def hasProperShape = this match {
		case CEmpty() => true
		/* The queue must have proper shape according to queue specs, and we cannot have a queue of empty lists */
		case CCons(h, t) => CatenableList.queueHasProperShapeIn(t)
	}

}

/* Companion object */
object CatenableList {

	/* Helpers */

	def linkAll[T](q: Queue[CatenableList[T]]): CatenableList[T] = {
		require(q.isDefined && queueHasProperShapeIn(q))
		q.tail match {
			case QEmpty() => q.head
			case qTail => q.head.link(linkAll(qTail))
		}
	} ensuring(res => q.forall(_.forall(res.contains(_))) && res.size == q.size)

	def sumTail[T](q: Queue[CatenableList[T]]): BigInt = {
		require(queueHasProperShapeIn(q))
		q match {
			case QEmpty() => 0
			case QCons(f, r) => sumInList(f, 0) + sumInList(r, 0)
		}
	} ensuring(_ >= 0)

	/* TODO: there were problems with foldleft / flatMap in leon, so we use those functions instead */

	private def sumInList[T](lst: List[CatenableList[T]], acc: BigInt): BigInt = {
		require(lst.forall(_.hasProperShape) && acc >= 0)
		lst match {
			case Nil() => acc
			case Cons(h, t) => sumInList(t, acc + h.size)
		}
	} ensuring(_ >= 0)

	def queueOfCatToContent[T](q: Queue[CatenableList[T]]): Set[T] = q match {
		case QEmpty() => Set()
		case QCons(l, r) => listOfCatToContent(l) ++ listOfCatToContent(r)
	}

	private def listOfCatToContent[T](l: List[CatenableList[T]]): Set[T] = l match {
		case Nil() => Set()
		case Cons(h, t) => h.content ++ listOfCatToContent(t)
	}

	def queueOfCatToList[T](q: Queue[CatenableList[T]]): List[T] = q match {
		case QEmpty() => Nil()
		case QCons(l, r) => listOfCatToList(l) ++ listOfCatToList(r)
	}

	private def listOfCatToList[T](l: List[CatenableList[T]]): List[T] = l match {
		case Nil() => Nil()
		case Cons(h, t) => h.toList ++ listOfCatToList(t)
	}

	/* Invariants */

	def queueHasProperShapeIn[T](q: Queue[CatenableList[T]]): Boolean = {
		q.hasProperShape && q.forall(x => x.isDefined && x.hasProperShape)
	}
}

case class CCons[T](h: T, t: Queue[CatenableList[T]]) extends CatenableList[T]
case class CEmpty[T]() extends CatenableList[T]
