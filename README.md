tasks
=====

    jozic@laptop ~/projects/tasks $ sbt
    Detected sbt version 0.12.0
    Starting sbt: invoke with -help for other options
    [info] Loading global plugins from /home/jozic/.sbt/plugins
    [info] Loading project definition from /home/jozic/projects/tasks/project
    [info] Set current project to tasks (in build file:/home/jozic/projects/tasks/)
    > run
    [info] Running ua.com.jozic.Tasks 
    
    > "task 1"
    1. task 1 : 
    > "task 2"
    1. task 1 : 
    2. task 2 : 
    > "tasks3+4
    > ^[[A^[[B^?
    > "tasks 3 + 4"
    1. task 1      : 
    2. task 2      : 
    3. tasks 3 + 4 : 
    > 
    > 
    > 
    > 
    > 
    > :l
    1. task 1      : 
    2. task 2      : 
    3. tasks 3 + 4 : 
    > 3
    1. task 1      : 
    2. task 2      : 
    3. tasks 3 + 4 : #
    > 2
    1. task 1      : 
    2. task 2      : #
    3. tasks 3 + 4 : #
    > 2
    1. task 1      : 
    2. task 2      : ##
    3. tasks 3 + 4 : #
    > 2
    1. task 1      : 
    2. task 2      : ###
    3. tasks 3 + 4 : #
    > :q
    buy
    [success] Total time: 81 s, completed Nov 17, 2012 5:21:40 PM
> 
