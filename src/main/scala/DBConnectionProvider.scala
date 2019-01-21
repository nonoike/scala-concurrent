case class DBConnection()

object DBConnectionProvider {
  private[this] val connectionHolder = new ThreadLocal[DBConnection] {
    override def initialValue(): DBConnection = DBConnection()
  }

  def provide: DBConnection = connectionHolder.get()
}
