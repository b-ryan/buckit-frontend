# buckit-frontend

A self-hosted budget app.

## Overview

GOALS:

* Budget functionality on par with YNAB
* Flexible reporting (no pie charts!)
* Usable on mobile
* Translatable (https://github.com/ptaoussanis/tower ?)
* Accessible (preferably -- determine difficulty?)
* Auditability -- be able to see who changed what and undo changes
* Multiple users & budgets

LATER (/ maybe never):

* Investment management (a la KMyMoney)

## Setup

```
npm install
bower install
```

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## Test

```
lein doo slimer test
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
