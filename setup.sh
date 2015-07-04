#
# Set up a development environment
# May or may not be needed in a particular context
#
#
# WARN: We got the following when switching to java-8-oracle:
# There was an error loading the BuildConfig: Cannot invoke method getAt() on null object
# java.lang.NullPointerException: Cannot invoke method getAt() on null object
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export GRAILS_HOME=/usr/local/grails-2.4.5
#export POSTXDB_CONF=/usr/local/etc/postxdb/postxdb-dev-config.properties
#export POSTXDB_CONF=/usr/local/etc/motrice/motrice-aug14-config.properties
#export MOTRICE_CONF=/usr/local/etc/motrice/motrice-test-sep14-config.properties
export POSTXDB_CONF=/usr/local/etc/skolverket/skoolform-config.properties
export M2_HOME=/usr/local/apache-maven-3.2.5

alias grails="$GRAILS_HOME/bin/grails -plain-output"
