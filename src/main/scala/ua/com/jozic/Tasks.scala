package ua.com.jozic

import annotation.tailrec
import java.io._
import io.Source

/**
 * @author jozic
 * @since 11/16/12
 */
class Tasks(tasksSeq: (String, String)*) {

  private val tasks = collection.mutable.ArrayBuffer[Task]()
  private var max = 0

  tasksSeq foreach {
    case (name, progress) => add(Task(name, progress.toInt))
  }

  def add(name: String): Tasks = add(Task(name, 0))

  def add(task: Task): Tasks = {
    tasks += task
    max = math.max(task.name.length, max)
    this
  }

  def inc(i: Int) = {
    for (t <- tasks.lift(i)) tasks(i) = t.inc
    this
  }

  def clear() = {
    tasks.clear()
    this
  }

  case class Task(name: String, progress: Int) {
    override def toString: String = name + " : " + "".padTo(progress, '#')

    def toStoreString: String = name + ":" + progress

    def inc = copy(progress = progress + 1)
  }

  override def toString: String = {

    def padToLeft(s: String, len: Int, c: Char) = "".padTo(len - s.length, c) + s

    val formatted = tasks.zipWithIndex map {
      case (t, i) => {
        val num = padToLeft((i+1).toString, tasks.size.toString.length, ' ')
        t.copy(name = num + ". " + t.name.padTo(max, '.'))
      }
    }
    formatted.mkString("\n")
  }

  def toStoreString: String = tasks.map(_.toStoreString).mkString("\n")
}

object Tasks extends App {

  val filename = "tasks.lst"
  val tasks = load()

  def load(): Tasks = {
    val loaded = if (new File(filename).exists())
      for {
        line <- Source.fromFile(filename).getLines()
        (name, progress) = line.splitAt(line.indexOf(':'))
      } yield (name, progress.drop(1))
    else Seq.empty
    new Tasks(loaded.toSeq: _*)
  }

  def save(tasks: Tasks): Tasks = {
    val writer: FileWriter = new FileWriter(filename)
    try {
      writer.write(tasks.toStoreString)
    } finally {
      writer.close()
    }
    tasks
  }

  def list() {
    println(tasks)
  }

  @tailrec
  def listen() {
    readLine("> ") match {
      case ":q" => println("buy")
      case ":l" => list(); listen()
      case ":drop" => tasks.clear(); save(tasks); listen()
      case n if n.matches("\\d+") =>
        tasks.inc(n.toInt - 1); save(tasks); list(); listen()
      case name if name.matches("\".+\"") =>
        tasks add name.stripPrefix("\"").stripSuffix("\""); save(tasks); list(); listen()
      case _ => listen()
    }
  }

  list()
  listen()
}