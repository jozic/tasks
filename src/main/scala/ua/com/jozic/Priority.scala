package ua.com.jozic

sealed trait Priority

object Priority {

  case object Low extends Priority {
    override def toString = "▼"
  }

  case object Medium extends Priority {
    override def toString = "•"
  }

  case object High extends Priority {
    override def toString = "▲"
  }

  implicit val ordering = new Ordering[Priority] {
    def compare(x: Priority, y: Priority) = values.indexOf(y) - values.indexOf(x)
  }

  val values = List(Low, Medium, High)

  def next(p: Priority) = values.lift(values.indexOf(p) + 1) getOrElse (p)

  def previous(p: Priority) = values.lift(values.indexOf(p) - 1) getOrElse (p)

  def resolve(s: String) = values.find(_.toString == s) getOrElse (Medium)
}