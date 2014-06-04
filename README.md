# What is CBLiteEktorp?

CBLiteEktorp is something to make it easy to access a [Couchbase Lite](https://github.com/couchbase/couchbase-lite-android) database via Ektorp.  

# How to use in your project

This assumes that `~/MyProject` is an Android Studio project which contains a settings.gradle file

*Step #1*: checkout CBLiteEktorp as a git submodule:

```
$ cd ~/MyProject
$ mkdir libraries && cd libraries
$ git submodule add https://github.com/couchbaselabs/couchbase-lite-android-ektorp.git
```

*Step #2*: add an entry to settings.gradle.

Before change:

```
include ':CouchChatAndroid', ':libraries:coucbase-lite-java-core', ':libraries:couchbase-lite-android'
```

After change:

```
include ':CouchChatAndroid', ':libraries:coucbase-lite-java-core', ':libraries:couchbase-lite-android', ':libraries:couchbase-lite-android-ektorp'
```

# Ektorp version

This uses a [forked version of Ektorp](https://github.com/couchbaselabs/Ektorp/tree/issue88_workaround) based on Ektorp 1.2.2.  
