import leon._
import leon.lang._
import leon.annotation._
import leon.collection._

/*
 * Implementation of Binomial Heap based on "Purely Functionnal Data Structure, Okasaki, P68+"
 * @author Maëlle Colussi
 * @author Mathieu Demarne
 */

case class BinHeap(trees: List[Tree]) {

  /* Lower-level API */

  def isEmpty = this.trees.isEmpty
  def isDefined = !this.isEmpty

  def insert(x: BigInt): BinHeap = {
    require(this.hasProperShape)
    val res: BinHeap = BinHeap(Ops.insTree(this.trees, Tree(x)))
    res
  } ensuring (res => res.size == this.size + 1 && res.hasProperShape)

  def merge(that: BinHeap): BinHeap = {
    require(this.hasProperShape && that.hasProperShape)
    val res: BinHeap = BinHeap(Ops.merge(this.trees, that.trees))
    res
  } ensuring (res => res.size == this.size + that.size && res.hasProperShape)

  def findMin: BigInt = {
    require(this.hasProperShape && this.isDefined)
    val (n, _) = Ops.getMin(this.trees)
    val res: BigInt = n.root
    res
  }

  def deleteMin: BinHeap = {
    require(this.hasProperShape && this.isDefined)
    val (n, ts) = Ops.getMin(this.trees)
    val res: BinHeap = BinHeap(Ops.merge(n.children.reverse, ts))
    res
  } ensuring (res => res.size == this.size - 1 && res.hasProperShape)

  def size: BigInt = {
    require(this.hasProperShape)
    val res: BigInt = Ops.size(this.trees)
    res
  } ensuring (_ >= 0)

  def content: Set[BigInt] = {
    require(this.hasProperShape)
    Ops.content(this.trees)
  }

  /* Helpers */

  private def hasProperShape = Ops.hasProperShape(this.trees)
}

/* Companion object */
object BinHeap {

  /* lower-leve API */

  def empty = BinHeap(Nil[Tree]())
  def apply(v: BigInt): BinHeap = empty.insert(v)
}

/* NOTE: contains list-based functions, since implicit classes are not supported by Leon */
object Ops {

  /* Invariants */

  def hasProperShape(c: List[Tree]): Boolean = hasMinHeapProp(c) && hasIncrRanks(c)

  /* Each binomial tree in a heap obeys the minimum-heap property: The key of a node is greater than or equal to the key of its parent. */
  def hasMinHeapProp(c: List[Tree]): Boolean = c.forall(_.hasProperShape)

  def hasIncrRanks(c: List[Tree]): Boolean = c match {
    case Nil() => true
    case Cons(t, Nil()) => t.rank >= 0
    case Cons(t1, ts @ Cons(t2, _)) => t1.rank >= 0 && t1.rank < t2.rank && hasIncrRanks(ts)
  }

  /* Intermediate shape that need to be verified for InsTree, since it follow specific constraints in term of rank. */
  def hasInsTreeProperShape(lhs: List[Tree], t1: Tree): Boolean = {
    val check1: Boolean = hasProperShape(lhs) && t1.hasProperShape
    val check2: Boolean = lhs match {
        case Nil() => true
        case Cons(t2, ts) if t1.rank < t2.rank || t1.rank == t2.rank => true
        case _ => false
      }
    val check: Boolean = check1 && check2
    check
  }

  /* Helpers */

  def merge(lhs: List[Tree], rhs: List[Tree]): List[Tree] = {
    require(hasProperShape(lhs) && hasProperShape(rhs))
    val res: List[Tree] = (lhs, rhs) match {
      case (t, Nil()) => t
      case (Nil(), t) => t
      case (Cons(t1, ts1), Cons(t2, ts2)) if t1.rank < t2.rank => t1 :: merge(ts1, t2 :: ts2)
      case (Cons(t1, ts1), Cons(t2, ts2)) if t1.rank > t2.rank => t2 :: merge(t1 :: ts1, ts2)
      case (Cons(t1, ts1), Cons(t2, ts2)) if t1.rank == t2.rank => insTree(merge(ts1, ts2), t1 link t2)
    }
    res
  } ensuring (res => hasProperShape(res)) // TODO: according to Specs, here res has NOT a proper shape

  def insTree(lhs: List[Tree], t1: Tree): List[Tree] = {
    require(hasInsTreeProperShape(lhs, t1))
    val res: List[Tree] = lhs match {
      case Nil() => t1 :: Nil()
      case Cons(t2, ts) if t1.rank < t2.rank => t1 :: t2 :: ts
      case Cons(t2, ts) => insTree(ts, t1 link t2)
    }
    res
  } ensuring (res => hasProperShape(res))

  def getMin(lhs: List[Tree]): (Tree, List[Tree]) = {
    require(!lhs.isEmpty && hasProperShape(lhs))
    lhs match {
      case Cons(t, Nil()) => (t, Nil())
      case Cons(t, ts) =>
        getMin(ts) match {
          case (tp, tsp) if t.root <= tp.root => (t, ts)
          case (tp, tsp) => (tp, t :: tsp)
        }
    }
  } ensuring (res => res._1.hasProperShape && hasProperShape(res._2))

  def size(lhs: List[Tree]): BigInt = {
    require(hasProperShape(lhs))
    val res: BigInt = lhs match {
      case Nil() => 0
      case Cons(t, ts) => t.size + size(ts)
    }
    res
  } ensuring (_ >= 0)

  def content(lhs: List[Tree]): Set[BigInt] = {
    require(hasProperShape(lhs))
    lhs match {
      case Nil() => Set()
      case Cons(t, ts) => t.content ++ content(ts)
    }
  }
}
