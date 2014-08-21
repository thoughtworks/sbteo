sbteo
=====


[![Build Status](https://travis-ci.org/thoughtworks/sbteo.png?branch=master)](http://travis-ci.org/thoughtworks/sbteo)

## What?

A compile server hosted in sbt for external tools

## Why?

We were implementing scala support in the cloud9 IDE, and wanted a server component to handle autocomplete and compile messages.

## How?

Add this plug-in to your global or project plugins.sbt

After running sbt (use the latest launcher):

```
sbteo:start
```

to stop, either kill the sbt session, or

```
sbteo:stop
```

This will make a websocket server available on `locahost:8888/sbt/`.  If you want to bind somewhere else, then 
 
```
SBTEO_ENDPOINT=<iface>:<port> sbt
```

or

```
sbt -Dsbteo.endpoint=<iface>:<port>
```

`<port>` will default to 8888, `<iface>` will default to localhost
 
## Credits

* [Quinton Anderson](https://github.com/quintona)
* [David Colls](https://github.com/safetydave)
* [Guillaume Massé](https://github.com/MasseGuillaume) 
* [Leonardo Borges](https://github.com/leonardoborges)

## Previous/Related work

* [ScalaIDE](https://github.com/themerius/ScalaIde)
* [ScalaKata](https://github.com/MasseGuillaume/ScalaKata)