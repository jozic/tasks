package ua.com.jozic

import annotation.tailrec
import java.io._
import io.Source
import java.util.Date
import java.text.SimpleDateFormat
import Priority._
import scala.util.control.Exception.ultimately

case class Task(name: String, progress: Int, lastUpdated: Date = new Date, priority: Priority = Medium) {

  val formatter = new SimpleDateFormat("MMM dd, yyyy")

  def date = formatter.format(lastUpdated)

  override def toString: String = s"$priority $name ( $date ) : ${"".padTo(progress, '#')}"

  def inc = copy(progress = progress + 1, lastUpdated = new Date)

  def << = copy(priority = previous(priority))

  def >> = copy(priority = next(priority))
}

class Tasks(tasksSeq: Array[Task]) {

  private val tasks = collection.mutable.ArrayBuffer[Task]()
  private var max = 0

  tasksSeq foreach add

  def this(tasksSeq: (String, String, String, String)*) = this(
    tasksSeq.toArray map {
      case (name, progress, date, priority) =>
        Task(name, progress.toInt, new Date(date.toLong), resolve(priority))
    }
  )

  def add(name: String): Tasks = add(Task(name, 0))

  def remove(i: Int) = {
    tasks.remove(i)
    this
  }

  def swap(i1: Int, i2: Int) = {
    if (tasks.isDefinedAt(i1) && tasks.isDefinedAt(i2)) {
      val tmp = tasks(i1)
      tasks(i1) = tasks(i2)
      tasks(i2) = tmp
    }
    this
  }

  def add(task: Task): Tasks = {
    tasks += task
    max = math.max(task.name.length, max)
    this
  }

  def inc(i: Int) = {
    for (t <- tasks.lift(i)) tasks(i) = t.inc
    this
  }

  def changePriority(i: Int, up: Boolean) = {
    for (t <- tasks.lift(i)) tasks(i) = if (up) t.>> else t.<<
    this
  }

  def clear() = {
    tasks.clear()
    this
  }

  def byDate = new Tasks(tasks.toArray.sortBy(_.lastUpdated).reverse)

  def byPriority = new Tasks(tasks.toArray.sortBy(_.priority))

  def byProgress = new Tasks(tasks.toArray.sortBy(-_.progress))

  override def toString: String = {

    def padToLeft(s: String, len: Int, c: Char) = "".padTo(len - s.length, c) + s

    val formatted = tasks.zipWithIndex map {
      case (t, i) => {
        val num = padToLeft((i + 1).toString, tasks.size.toString.length, ' ')
        t.copy(name = s"$num.${t.name.padTo(max, '.')}")
      }
    }
    formatted.mkString("\n")
  }

}

object Tasks extends App {

  val filename = "tasks.lst"
  val tasks = load()

  def unEscapeColon(s: String) = s.replaceAll("__colon__", ":")

  def escapeColon(s: String) = s.replaceAll(":", "__colon__")

  def load(): Tasks = {
    val loaded = if (new File(filename).exists())
      for {
        line <- Source.fromFile(filename).getLines()
        (name, progressAndDateAndPriorityString) = line.splitAt(line.indexOf(':'))
        progressAndDatePriority = progressAndDateAndPriorityString.drop(1)
        (progress, dateAndPriorityString) = progressAndDatePriority.splitAt(progressAndDatePriority.indexOf(':'))
        dateAndPriority = dateAndPriorityString.drop(1)
        (date, priorityString) = dateAndPriority.splitAt(dateAndPriority.indexOf(':'))
      } yield (unEscapeColon(name), progress, date, priorityString.drop(1))
    else Seq.empty
    new Tasks(loaded.toSeq: _*)
  }

  def toStoreString(task: Task): String = s"${escapeColon(task.name)}:${task.progress}:${task.lastUpdated.getTime}:${task.priority.toString}"

  def toStoreString(tasks: Tasks): String = tasks.tasks.map(toStoreString).mkString("\n")

  def save(tasks: Tasks): Tasks = {

    val writer: FileWriter = new FileWriter(filename)
    ultimately(writer.close()) {
      writer.write(toStoreString(tasks))
    }
    tasks
  }

  def list(t: Tasks = tasks) {
    println(t)
  }

  def saveAndList(t: Tasks = tasks) {
    list(save(t))
  }

  def usage: String =
    """ Tracks a number of ongoing small tasks with the progress relative to each other
    | usage:
    :q => exit
    :l => show list
    :h => show this message
    :drop => clear tasks list in file
    :date => show list sorted by date
    :prio => show list sorted by priority
    :prg => show list sorted by progress
    `index` => increase the progress of task identified by index.
          Example: 5
    `index`>> => increase the priority of the task identified by index.
          Example: 5>>
    `index`<< => decrease the priority of the task identified by index.
          Example: 5<<
    !`index` => removes the task identified by index.
          Example: !5
    `index1`><`index2` => swap tasks identified by index1 and index2.
          Example: 2><5
    "some text" => creates new task with the name `some text` and appends it to the tasks list.
          Example: "my new task"
    """.stripMargin

  @tailrec
  def listen() {
    val number = "\\d+"

    def toInt(n: String) = n.toInt - 1
    readLine("> ") match {
      case ":q" => println("buy"); sys.exit()
      case ":l" => list()
      case ":h" => println(usage)
      case ":drop" => save(tasks.clear())
      case ":date" => list(tasks.byDate) //todo fix order
      case ":prio" => list(tasks.byPriority) //todo fix order
      case ":prg" => list(tasks.byProgress) //todo fix order
      case n if n.matches(number) => saveAndList(tasks.inc(toInt(n)))
      case n if n.matches(number + ">>") => saveAndList {
        tasks.changePriority(toInt(n.dropRight(2)), up = true)
      }
      case n if n.matches(number + "<<") => saveAndList {
        tasks.changePriority(toInt(n.dropRight(2)), up = false)
      }
      case n if n.matches("!" + number) => saveAndList {
        tasks.remove(toInt(n.drop(1)))
      }
      case n if n.matches(number + "><" + number) => saveAndList {
        val (n1, n2) = n.splitAt(n.indexOf("><"))
        tasks.swap(toInt(n1), toInt(n2.drop(2)))
      }
      case name if name.matches("\".+\"") => saveAndList {
        tasks add name.stripPrefix("\"").stripSuffix("\"")
      }
      case _ =>
    }
    listen()
  }

  list()
  listen()
}