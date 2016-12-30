![Mighty Fine Memes](logo.png)

A front-end for the [memegen.link](https://memegen.link/) [API](https://github.com/jacebrowning/memegen) written in ClojureScript using [re-frame](https://github.com/Day8/re-frame).

[Check it out!](http://www.mightyfinememes.com/)

## Development Mode

### Start Cider from Emacs:

Put this in your Emacs config file:

```
(setq cider-cljs-lein-repl "(do (use 'figwheel-sidecar.repl-api) (start-figwheel!) (cljs-repl))")
```

Navigate to a clojurescript file and start a figwheel REPL with `cider-jack-in-clojurescript` or (`C-c M-J`)

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```


### SASS Compilation
Using [lein-sassy](https://github.com/vladh/lein-sassy)

Compile files once
```
use lein sass once
```

Watch files for changes

```
lein sass watch
```

Remove generated files, run

```
lein sass clean
```
