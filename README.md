tasks
=====
    jozic@laptop ~/projects/tasks $ sbt
    Starting sbt: invoke with -help for other options
    [info] Loading global plugins from /home/jozic/.sbt/plugins
    [info] Set current project to tasks (in build file:/home/jozic/projects/tasks/)
    > run
    [info] Running ua.com.jozic.Tasks

    > "tasks project"
    • 1.tasks project ( Dec 01, 2012 ) :
    > "quick patch project"
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    > "sbt-idea project"
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    > "scala continuations"
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    > "compilers course"
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    • 5.compilers course... ( Dec 01, 2012 ) :
    > "clojure"
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    • 5.compilers course... ( Dec 01, 2012 ) :
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 5>>
    • 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) :
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 1<<
    ▼ 1.tasks project...... ( Dec 01, 2012 ) :
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) :
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 1
    ▼ 1.tasks project...... ( Dec 01, 2012 ) : #
    • 2.quick patch project ( Dec 01, 2012 ) :
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) :
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 2
    ▼ 1.tasks project...... ( Dec 01, 2012 ) : #
    • 2.quick patch project ( Dec 01, 2012 ) : #
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) :
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 5
    ▼ 1.tasks project...... ( Dec 01, 2012 ) : #
    • 2.quick patch project ( Dec 01, 2012 ) : #
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) : #
    • 6.clojure............ ( Dec 01, 2012 ) :
    > 5
    ▼ 1.tasks project...... ( Dec 01, 2012 ) : #
    • 2.quick patch project ( Dec 01, 2012 ) : #
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) : ##
    • 6.clojure............ ( Dec 01, 2012 ) :
    >
    >
    > :l
    ▼ 1.tasks project...... ( Dec 01, 2012 ) : #
    • 2.quick patch project ( Dec 01, 2012 ) : #
    • 3.sbt-idea project... ( Dec 01, 2012 ) :
    • 4.scala continuations ( Dec 01, 2012 ) :
    ▲ 5.compilers course... ( Dec 01, 2012 ) : ##
    • 6.clojure............ ( Dec 01, 2012 ) :
    > :h
     Tracks a number of ongoing small tasks with the progress relative to each other
     usage:
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

    > :q
    buy
    [success] Total time: 222 s, completed Dec 1, 2012 11:41:51 PM
    >
