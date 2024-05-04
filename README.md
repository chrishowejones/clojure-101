# Clojure 101

This project is intended to be a demo of Clojure and ClojureScript.

The Clojurescript is bootstrapped from the Clojure handler namespace
(clojure-101.handler).

There are 3 distinct parts to the project.

1. ClojureScript home and about pages that are text and demonstrate
   using the secretary library to handle routing for a single page
   application. See src/cljs/clojure_101/core.cljs
2. ClojureScript TicTacToe game - see src/cljs/clojure_101/tictactoe/
3. Clojure API that serves up two resources (/api/people and
   /api/popular-studio). See src/clj/clojure_101/api.clj

## Dependencies

Clojure the language is downloaded as a dependency for this project
therefore only Java 1.7+ and Leiningen (https://leiningen.org/) need to be pre-installed.

## Usage

You can run the project in development mode using the Leiningen build
tool (https://leiningen.org/).

The easiest way to do this is to issue the following lein command from
the project root directory (the directory this README is in after
cloning locally).

    $ lein figwheel

You can then access the project on this URL http://localhost:3449/

This actually bootstraps the project in development mode which means
editing the source will force a reload that you will see in the
browser if it's open at the time.

The other way of running the project in development mode is to run
figwheel (the ClojureScript hot reloading tool) manually in the REPL.

     $ lein repl
     ...
     clojure-101.repl=> (require 'figwheel-sidecar.repl-api)
     clojure-101.repl=> (figwheel-sidecar.repl-api/start-figwheel!)
     clojure-101.repl=> (figwheel-sidecar.repl-api/cljs-repl)

If you want to compile the project into a standalone JAR that can be
run or deployed in any environment with a Java Runtime Environment
(+1.7) you need to issue the following command:

     $ lein uberjar

Once compiled into an uberjar you can run the project using:

     $ java -jar <location of uberjar>/clojure101.jar

E.g. from the target directory the uberjar compiles too:

     $ java -jar target/clojurem101.jar

When built as an uberjar you can access the UI using the URL -
http://localhost:3000/

Once compiled as an uberjar, hot reloading is obviously not supported
so code changes will need a compile to be applied.

## License

Copyright © 2017 Chris Howe-Jones

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
