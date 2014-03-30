package ua.com.jozic

import annotation.tailrec
import java.io._
import io.Source
import java.util.Date
import java.text.SimpleDateFormat
import Priority._
import scala.util.control.Exception.ultimately
import jline.console.ConsoleReader
import jline.console.completer.{ArgumentCompleter, StringsCompleter, AggregateCompleter}
import java.util
import scala.collection.JavaConverters._

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

  def remove(i: Int) = {
    for (_ <- tasks.lift(i)) tasks.remove(i)
    this
  }

  def swap(i1: Int, i2: Int) = {
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

  def size = tasks.size

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

  val listArgCommands = Seq(":use", ":drop")

  val noArgsCommands = Seq(":q", ":l", ":lists", ":h", ":clr", ":date", ":prio", ":prg")

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

  def unEscapeColon(s: String) = s.replaceAll("__colon__", ":")

  def escapeColon(s: String) = s.replaceAll(":", "__colon__")

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

  def list(t: Tasks = tasks) {
    println(t)
  }

  def listLists() {
    println(availableLists.mkString("\n"))
  }

  private def availableLists: Array[String] = {
    val files: Array[File] = new File(".").listFiles(new FilenameFilter {
      def accept(dir: File, name: String): Boolean = name.endsWith(".lst")
    })
    files map (_.getName.stripSuffix(EXTENSION))
  }

  def saveAndList(t: Tasks = tasks) {
    list(save(t))
  }

  def use(fileName: String) {
    new File(fileName + EXTENSION) match {
      case f if f.exists() && f.isFile =>
        tasks = load(f.getName)
        list()
      case notFound => readLine(s"List $fileName is not found. Do you want to create it? (y/n):") match {
        case "y" if notFound.createNewFile() => use(fileName)
        case "y" => println("Can't create new list. Using old list...")
        case _ => println("Using old list...")
      }
    }
  }

  def drop(fileName: String) {
    if (tasks.listName == fileName)
      println("Can't drop current list. Switch to another list with :use first.")
    else new File(fileName + EXTENSION) match {
      case f if f.exists() && f.isFile && f.delete() => println(s"List $fileName has been dropped.")
      case f if f.exists() && f.isFile => println(s"Can't drop list $fileName :(")
      case notFound => println(s"List $fileName is not found")
    }
  }

  def usage: String =
    """ Tracks a number of ongoing small tasks with the progress relative to each other
      | usage:
    :q => exit
    :l => show current list
    :lists => show all lists in directory
    :use <list_name> => load new list identified by list_name
      Example: :use work
    :drop <list_name> => drop list identified by list_name
    :h => show this message
    :clr => clear current tasks list in file
    :date => show current list sorted by date
    :prio => show current list sorted by priority
    :prg => show current list sorted by progress
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
    def spaces(s: String) = s"\\s*$s\\s*"

    val Quit = spaces(":q")
    val ListItems = spaces(":l")
    val ListLists = spaces(":lists")
    val Help = spaces(":h")
    val ClearList = spaces(":clr")
    val SortByDate = spaces(":date")
    val SortByPriority = spaces(":prio")
    val SortByProgress = spaces(":prg")
  }

  @tailrec
  def listen() {
    val number = "\\d+"

    def toInt(n: String) = n.toInt - 1

    import Commands._
    reader.readLine(s"${tasks.listName}> ") match {
      case q if q.matches(Quit) => println("buy"); sys.exit()
      case l if l.matches(ListItems) => list()
      case lists if lists.matches(ListLists) => listLists()
      case useName if useName.matches(":use \\S+") => use(useName.stripPrefix(":use "))
      case dropName if dropName.matches(":drop \\S+") => drop(dropName.stripPrefix(":drop "))
      case h if h.matches(Help) => println(usage)
      case clr if clr.matches(ClearList) => save(tasks.clear())
      case date if date.matches(SortByDate) => list(tasks.byDate) //todo fix order
      case prio if prio.matches(SortByPriority) => list(tasks.byPriority) //todo fix order
      case prg if prg.matches(SortByProgress) => list(tasks.byProgress) //todo fix order
      case n if n.matches(number) => saveAndList(tasks.inc(toInt(n)))
      case n if n.matches(number + ">>") => saveAndList {
        tasks.changePriority(toInt(n.dropRight(2)), up = true)
      }
      case n if n.matches(number + "<<") => saveAndList {
        tasks.changePriority(toInt(n.dropRight(2)), up = false)
      }
      case n if n.matches("-" + number) => saveAndList {
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