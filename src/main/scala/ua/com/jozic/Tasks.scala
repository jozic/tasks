package ua.com.jozic

import annotation.tailrec

/**
 * @author jozic
 * @since 11/16/12
 */
class Tasks {

  private val tasks = collection.mutable.ArrayBuffer[Task]()
  private var max = 0

  def +(name: String) = {
    tasks += Task(name, 0)
    max = math.max(name.length, max)
    this
  }

  def inc(i: Int) = {
    for (t <- tasks.lift(i)) tasks(i) = t.inc
    this
  }

  def list() {
    println(this)
  }

  case class Task(name: String, progress: Int) {
    override def toString: String = name + " : " + "".padTo(progress, '#')

    def inc = copy(progress = progress + 1)
  }

  override def toString: String = {
    val formatted = tasks.zipWithIndex map {
      case (t, i) => t.copy(name = (i + 1) + ". " + t.name.padTo(max, ' '))
    }
    formatted.mkString("\n")
  }
}

object Tasks extends App {

  val tasks = new Tasks

  tasks + "small task 1"
  tasks + "task 2"
  tasks + "task 3"
  tasks + "huge task 4"

  tasks inc 1
  tasks inc 2

  tasks.list()

  @tailrec
  def listen() {
    readLine("> ") match {
      case ":q" => println("buy")
      case ":l" => tasks.list(); listen()
      case n if n.matches("\\d+") =>
        tasks.inc(n.toInt - 1); tasks.list(); listen()
      case _ => listen()
    }
  }

  listen()
}