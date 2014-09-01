package com.daodecode.tasks

import java.io._
import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.daodecode.tasks.Priority._
import jline.console.ConsoleReader
import jline.console.completer.{AggregateCompleter, ArgumentCompleter, StringsCompleter}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.control.Exception.ultimately
import scala.util.matching.Regex

case class Task(name: String, progress: Int, lastUpdated: Date = new Date, priority: Priority = Medium) {

  val formatter = new SimpleDateFormat("MMM dd, yyyy")

  def date = formatter.format(lastUpdated)

  override def toString: String = s"$priority $name ( $date ) : ${"".padTo(progress, '#')}"

  def inc = copy(progress = progress + 1, lastUpdated = new Date)

  def << = copy(priority = previous(priority))

  def >> = copy(priority = next(priority))
}

class Tasks(val listName: String, tasksSeq: Array[Task]) {

  private val tasks = collection.mutable.ArrayBuffer[Task]()
  private var max = 0

  tasksSeq foreach add

  def this(listName: String, tasksSeq: (String, String, String, String)*) = this(listName,
    tasksSeq.toArray map {
      case (name, progress, date, priority) =>
        Task(name, progress.toInt, new Date(date.toLong), resolve(priority))
    }
  )

  def add(name: String): Tasks = add(Task(name, 0))

  def remove(i: Int): Tasks = {
    for (_ <- tasks.lift(i)) tasks.remove(i)
    this
  }

  def swap(i1: Int, i2: Int): Tasks = {
    for {
      t1 <- tasks.lift(i1)
      t2 <- tasks.lift(i2)
    } {
      tasks(i2) = t1
      tasks(i1) = t2
    }
    this
  }

  def add(task: Task): Tasks = {
    tasks += task
    max = math.max(task.name.length, max)
    this
  }

  def inc(i: Int): Tasks = {
    for (t <- tasks.lift(i)) tasks(i) = t.inc
    this
  }

  def changePriority(i: Int, up: Boolean): Tasks = {
    for (t <- tasks.lift(i)) tasks(i) = if (up) t.>> else t.<<
    this
  }

  def clear(): Tasks = {
    tasks.clear()
    this
  }

  def size: Int = tasks.size

  def byDate = new Tasks(listName, tasks.toArray.sortBy(_.lastUpdated).reverse)

  def byPriority = new Tasks(listName, tasks.toArray.sortBy(_.priority))

  def byProgress = new Tasks(listName, tasks.toArray.sortBy(-_.progress))

  override def toString: String = {

    def padToLeft(s: String, len: Int, c: Char) = "".padTo(len - s.length, c) + s

    val formatted = tasks.zipWithIndex map {
      case (t, i) =>
        val num = padToLeft((i + 1).toString, tasks.size.toString.length, ' ')
        t.copy(name = s"$num.${t.name.padTo(max, '.')}")
    }
    formatted.mkString("\n")
  }

}

object Tasks extends App {

  val EXTENSION = ".lst"
  val DEFAULT_FILE_NAME = s"tasks$EXTENSION"
  private var tasks = load(DEFAULT_FILE_NAME)

  val listArgCommands = Seq(Commands.Use, Commands.Drop).map(_.name)

  val noArgsCommands = Seq(Commands.Quit, Commands.ListItems, Commands.ListLists, Commands.Help,
    Commands.ClearList, Commands.SortByDate, Commands.SortByPriority, Commands.SortByProgress).map(_.name)

  val reader = {
    val reader: ConsoleReader = new ConsoleReader()
    reader.addCompleter(new AggregateCompleter(new StringsCompleter(noArgsCommands.asJava),
      new ArgumentCompleter(new StringsCompleter(listArgCommands.asJava), new ListNamesCompleter)))
    reader
  }

  private class ListNamesCompleter extends StringsCompleter {
    override def complete(buffer: String, cursor: Int, candidates: util.List[CharSequence]) = {
      getStrings.clear()
      getStrings.addAll(availableLists.toSeq.asJava)
      super.complete(buffer, cursor, candidates)
    }
  }

  private def unEscapeColon(s: String) = s.replaceAll("__colon__", ":")

  private def escapeColon(s: String) = s.replaceAll(":", "__colon__")

  def load(filename: String): Tasks = {
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
    new Tasks(filename.stripSuffix(EXTENSION), loaded.toSeq: _*)
  }

  def toStoreString(task: Task): String = s"${escapeColon(task.name)}:${task.progress}:${task.lastUpdated.getTime}:${task.priority.toString}"

  def toStoreString(tasks: Tasks): String = tasks.tasks.map(toStoreString).mkString("\n")

  def save(tasks: Tasks): Tasks = {
    val writer: FileWriter = new FileWriter(tasks.listName + EXTENSION)
    ultimately(writer.close()) {
      writer.write(toStoreString(tasks))
    }
    tasks
  }

  def list(t: Tasks = tasks): Unit = {
    println(t)
  }

  def listLists(): Unit = {
    println(availableLists.mkString("\n"))
  }

  private def availableLists: Array[String] = {
    val files: Array[File] = new File(".").listFiles(new FilenameFilter {
      def accept(dir: File, name: String): Boolean = name.endsWith(".lst")
    })
    files map (_.getName.stripSuffix(EXTENSION))
  }

  def saveAndList(t: Tasks = tasks): Unit = {
    list(save(t))
  }

  def use(fileName: String): Unit = {
    new File(fileName + EXTENSION) match {
      case f if f.exists() && f.isFile =>
        tasks = load(f.getName)
      case notFound => reader.readLine(s"List $fileName is not found. Do you want to create it? (y/n):") match {
        case "y" if notFound.createNewFile() => use(fileName)
        case "y" => println("Can't create new list. Using old list...")
        case _ => println("Using old list...")
      }
    }
  }

  def drop(fileName: String): Unit = {
    if (tasks.listName == fileName)
      println(s"Can't drop current list. Switch to another list with ${Commands.Use} first.")
    else new File(fileName + EXTENSION) match {
      case f if f.exists() && f.isFile && f.delete() => println(s"List $fileName has been dropped.")
      case f if f.exists() && f.isFile => println(s"Can't drop list $fileName :(")
      case notFound => println(s"List $fileName is not found")
    }
  }

  def usage: String =
    s""" Tracks a number of ongoing small tasks with the progress relative to each other
      | usage:
    ${Commands.Quit} => exit
    ${Commands.ListItems} => show current list
    ${Commands.ListLists} => show all lists in directory
    ${Commands.Use} <list_name> => load new list identified by list_name
      Example: ${Commands.Use} work
    ${Commands.Drop} <list_name> => drop list identified by list_name
    ${Commands.Help} => show this message
    ${Commands.ClearList} => clear current tasks list in file
    ${Commands.SortByDate} => show current list sorted by date
    ${Commands.SortByPriority} => show current list sorted by priority
    ${Commands.SortByProgress} => show current list sorted by progress
    `index` => increase the progress of task identified by index.
          Example: 5
    `index`>> => increase the priority of the task identified by index.
          Example: 5>>
    `index`<< => decrease the priority of the task identified by index.
          Example: 5<<
    -`index` => removes the task identified by index.
          Example: -5
    `index1`><`index2` => swap tasks identified by index1 and index2.
          Example: 2><5
    "some text" => creates new task with the name `some text` and appends it to the tasks list.
          Example: "my new task"
    """.stripMargin


  object Commands {
    val Spaces = "\\s*"
    val Number = "(\\d+)"

    def spaces(s: String*) = s.mkString(Spaces, "\\s+", Spaces)

    case class Command(name: String, args: String*) extends Regex(spaces(name +: args: _*)) {
      override def toString() = name
    }

    val Quit = Command(":q")
    val ListItems = Command(":l")
    val ListLists = Command(":lists")
    val Help = Command(":h")
    val ClearList = Command(":clr")
    val SortByDate = Command(":date")
    val SortByPriority = Command(":prio")
    val SortByProgress = Command(":prg")
    val Use = Command(":use", "(\\w+)")
    val Drop = Command(":drop", "(\\w+)")

    val MakeProgress = spaces(Number).r
    val IncPriority = spaces(s"$Number>>").r
    val DecPriority = spaces(s"$Number<<").r
    val RemoveItem = spaces(s"-$Number").r
    val SwapItems = spaces(s"$Number><$Number").r
    val NewItem = spaces("\"(.+)\"").r
  }

  @tailrec
  def listen(): Unit = {
    def toInt(n: String) = n.toInt - 1

    import Commands._

    reader.readLine(s"${tasks.listName}> ") match {
      case Quit() => println("buy"); sys.exit()
      case ListItems() => list()
      case ListLists() => listLists()
      case Use(listName) => use(listName); list()
      case Drop(listName) => drop(listName)
      case Help() => println(usage)
      case ClearList() => save(tasks.clear())
      case SortByDate() => list(tasks.byDate) //todo fix order
      case SortByPriority() => list(tasks.byPriority) //todo fix order
      case SortByProgress() => list(tasks.byProgress) //todo fix order
      case MakeProgress(n) => saveAndList(tasks.inc(toInt(n)))
      case IncPriority(n) => saveAndList(tasks.changePriority(toInt(n), up = true))
      case DecPriority(n) => saveAndList(tasks.changePriority(toInt(n), up = false))
      case RemoveItem(n) => saveAndList(tasks.remove(toInt(n)))
      case SwapItems(n1, n2) => saveAndList(tasks.swap(toInt(n1), toInt(n2)))
      case NewItem(name) => saveAndList(tasks.add(name))
      case _ =>
    }
    listen()
  }

  args.lift(0).foreach(use)
  list()
  listen()
}