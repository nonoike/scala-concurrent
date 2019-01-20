object CheckThenAct extends App {
  for (_ <- 1 to 100) {
    new Thread(() => println(SingletonProvider.get)).start()
  }
}

object SingletonProvider {
  //  private[this] var singleton: BigObject = _
  //
  //  def get: BigObject = this.synchronized {
  //    if (singleton == null) {
  //      singleton = new BigObject
  //    }
  //    singleton
  //  }

  lazy val get: BigObject = new BigObject()
}

class BigObject() {
  Thread.sleep(1000)
}
