JAVAC=javac

srcdir=src
bindir=bin
libdir=lib

sources=$(wildcard $(srcdir)/*.java)
classes=$(patsubst $(srcdir)/%.java,bin/%.class,$(sources))

all: $(classes)

bin/%.class: src/%.java
	$(JAVAC) -d $(bindir) -sourcepath $(srcdir) -classpath $(libdir)/*.jar  $<

clean:
	$(RM) bin/*.class

.PHONY: compile clean
