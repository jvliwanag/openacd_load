JAVAC=javac

srcdir=src
bindir=bin
libdir=lib

sources=$(shell find $(srcdir) -type f -name '*.java')
classes=$(patsubst $(srcdir)/%.java,bin/%.class,$(sources))

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

jars=$(wildcard $(libdir)/*.jar)
classpath=$(subst $(SPACE),:,$(jars))

all: $(classes)

bin/%.class: src/%.java
	$(JAVAC) -d $(bindir) -sourcepath $(srcdir) -classpath $(classpath)  $<

clean:
	$(RM) bin/*.class

.PHONY: compile clean
