# Make file for the jiag project...

ifndef JAVAC
JAVAC := javac
endif

# define JAR to fastjar if you like...
ifndef JAR
JAR := jar
endif

# to allow compile with "computr> JCFLAGS=-O make dist"
ifndef JCFLAGS
JCFLAGS := -encoding ISO-8859-1 -Xlint:unchecked -deprecation
endif

ifndef JAVADOC
JAVADOC := javadoc
endif

ifndef JDFLAGS
JDFLAGS := -windowtitle "PRTree" -doctitle "PRTree" -use -version -author \
-encoding ISO-8859-1 \
-link http://java.sun.com/javase/6/docs/api
endif

# Source file base directory
SRCDIR := src

# Class file destination base directory
ifndef CLASSDIR
CLASSDIR := classes
endif

# Documentation destination base directory
DOCDIR := javadoc

# Jar destination directory.
JARDIR := jars

# Packages are taken from this base.
JARPACKAGEBASE := org

# Set separator based on OS (: or ;)
ifndef OS
SEP :=:
PROJDIR:=$(PWD)
else
SEP :=;
PROJDIR:=.
endif

# Where to look for source files. 
SRCPATH := "$(SRCDIR)"

LIBS := $(wildcard $(PROJDIR)/external_libs/*.jar)
CLASSPATH := .$(SEP)$(CLASSPATH)
ifneq ($(LIBS), )
CLASSPATH := $(LIBS)$(SEP)$(CLASSPATH)
endif
CLASSPATH := $(shell echo "$(CLASSPATH)" | sed -e 's/jar /jar$(SEP)/g' | sed -e 's/zip /zip$(SEP)/g')
CLASSPATH := "$(CLASSPATH)"

CLASSPATHVAR := -classpath $(CLASSPATH)

# standard java compilation..
JAVACOMPILE := $(JAVAC) $(JCFLAGS) $(CLASSPATHVAR) -sourcepath $(SRCPATH) -d $(CLASSDIR)

# Package list
include .packagelist

###########################################################
# Don't change anything below unless you know what you are doing.
###########################################################
SPACKAGES := $(addsuffix .pkg,$(PACKAGES))
SRCDIRS := $(addprefix $(SRCDIR)/,$(subst .,/,$(PACKAGES)))
SRCDIRS := $(addsuffix /,$(SRCDIRS))
VPATH := $(SRCDIR)
SOURCES := $(wildcard $(addsuffix *.java,$(SRCDIRS)))
CLASSES := $(patsubst $(SRCDIR)/%,$(CLASSDIR)/%,$(SOURCES))
CLASSES := $(CLASSES:.java=.class)

empty=

# Check if the class list is non-empty, if not compile the classes we need. 
# If we get an error we remove all classes that should have been generated. 
# We want to use -f since we ignore non-existing files, that also mean that
# we have to make sure that the compile fails => false 
COND_COMPILE=\
if [ -s .classes_to_compile ]; \
then \
    ($(JAVACOMPILE) @.classes_to_compile && echo -n > .classes_to_compile) || \
    (rm -f `echo \`cat .classes_to_compile\` | sed 's/\.java/.class/g' | sed 's/$(SRCDIR)/$(CLASSDIR)/g'` && false) \
fi

.PHONY:	code deps .deps packages .packagelist all cond_compile jars

all:
	$(MAKE) -s realclean
	$(MAKE) -s packages
	$(MAKE) -s everything
	$(MAKE) -s deps
	$(MAKE) -s jars

help:
	@echo " - Generic Java makefile -"

# include dependancies after target 'all'.
include .deps

# compile one java file.
$(CLASSDIR)/%.class : %.java
	@echo $<
	@echo $< >> .classes_to_compile
	@if [ -f $@ ]; then touch $@; fi # need to set time on target file here.

# compile a whole package with one javac command...
%.pkg :
	@echo $(patsubst %.pkg,%,$@)
	@$(JAVACOMPILE) $(wildcard $(SRCDIR)/$(subst .,/,$(patsubst %.pkg,%,$@))/*.java)

.deps:
	@touch .deps

clean-deps:
	@echo -n > .deps

# find jar files dependancies.
# TODO! currently needs a class directory structure...
# create one jar file per top level package, dependant on all files in it
# or sub packages.
deps:	clean-deps
	@echo -n 'jars/prtree.jar:	' >> .deps
	@find classes -type f -name \*.class -exec echo -n '{} ' \; | perl -npe 's/\$$/\$$\$$/g' >> .deps
	@echo >> .deps
	@echo >> .deps
	@echo JARS := jars/prtree.jar >> .deps

# build the package file...
packages: packages.touch
	@echo "PACKAGES := \\" > .packagelist
	@(cd $(SRCDIR); find org ! -name \.svn -name \*.java | \
          sed -e 's/\/[^/]*$$/ \\/' | sed -e 's/\//\./g' | sort -u) >> .packagelist
	@rm packages.touch

packages.touch:
	@touch packages.touch

.packagelist:
	@touch .packagelist

cond_compile: 
	@echo compiling classes: 
	$(COND_COMPILE)

%.jar : 
	@$(COND_COMPILE)
	@echo building $@
	@$(JAR) -cmf Manifest.mf $@ -C $(CLASSDIR) $(JARPACKAGEBASE)

realclean:
	@rm -rf $(CLASSDIR)/*
	@rm -rf $(JARDIR)/*
	@rm -rf $(DOCDIR)/*

everything:
	@echo $(SOURCES) | tr ' ' '\n' > .allclasses.txt
	@$(JAVACOMPILE) @.allclasses.txt

ifeq ($(SOURCES),$(empty))

code: nosource

doc: nosource

servlets: nosource

junit: nosource

else

classfile: 
	@echo -n > .classes_to_compile

classdir: 
	@if [ ! -d $(CLASSDIR) ]; then  mkdir -p $(CLASSDIR); fi

code:	classfile classdir $(CLASSES)
	@$(COND_COMPILE)
	@true

# create the directory if it does not exist...
docdirectory:
	@if [ ! -d $(DOCDIR) ]; then  mkdir -p $(DOCDIR); fi

doc:  $(SOURCES) docdirectory
# create directory if it does not exist
	@$(JAVADOC) $(JDFLAGS) -classpath $(CLASSPATH) -sourcepath $(SRCDIR) -d $(DOCDIR) $(PACKAGES)
	@echo javadoc generated.....

doc-quiet:  $(SOURCES)
	@echo generating javadoc...
	@mkdir -p $(DOCDIR)
	@$(JAVADOC) $(JDFLAGS) -classpath $(CLASSPATH) -sourcepath $(SRCDIR) -d $(DOCDIR) $(PACKAGES) > /dev/null
	@echo javadoc generated.

endif

# create the directory if it does not exists... having a target here is
# evil, since the directory name clashes with the "jars" target.
jardirectory:
	@if [ ! -d $(JARDIR) ]; then  mkdir -p $(JARDIR); fi

jars:	code jardirectory $(JARS)
	@true

ifeq ($(MAKELEVEL),0)

%::
	@make --no-print-directory PACKAGES=$* code

endif

nosource:
	@echo Sources for package $(PACKAGES) not found
	@exit 42
