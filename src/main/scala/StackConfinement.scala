import scala.collection.mutable
import scala.util.Random

object StackConfinement {
  def randomSortedThreeNums: List[Int] = {
    val priorityQueue = new mutable.PriorityQueue[Int]()
    val random = new Random()
    priorityQueue += random.nextInt()
    priorityQueue += random.nextInt()
    priorityQueue += random.nextInt()
    priorityQueue.clone.dequeueAll // cloneすることによりスレッド拘束を実現
  }

}
