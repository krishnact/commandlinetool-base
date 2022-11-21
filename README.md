#To build and deploy locally
gradlew clean jar publishToMavenLocal

# commandlinetool-base
A library to base command line tools.

**Step 1. Add the JitPack repository to your build file**
```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
```

**Step 2. Add the dependency**
```
<dependency>
	    <groupId>com.github.krishnact</groupId>
	    <artifactId>commandlinetool-base</artifactId>
	    <version>0.4.12</version>
</dependency>
```
See jitpack to learn how to used in other build tools.
https://jitpack.io/#krishnact/commandlinetool-base/0.4.9

Example: Writing a simple URL Size counter, in Groovy.
```

    @Grapes([
        @GrabResolver(name='jitpack.io', root='https://jitpack.io'),
        @Grab('org.slf4j:slf4j-log4j12:1.7.7'),
        @Grab('com.h2database:h2:1.4.196'),
        @Grab('com.github.krishnact:commandlinetool-base:0.4.12'),
        @GrabExclude(group = 'org.codehaus.groovy', module='groovy-sql') ,
        @GrabExclude(group = 'org.codehaus.groovy', module='groovy-cli-commons')  ,
        @GrabExclude(group = 'org.codehaus.groovy', module='groovy-json')         ,
        @GrabExclude(group = 'org.codehaus.groovy', module='groovy-xml')           ,
        @GrabExclude(group = 'org.codehaus.groovy', module='groovy-templates')
    ])
    import org.himalay.commandline.Option;
    import org.himalay.commandline.CLTBase;
    import groovy.cli.commons.OptionAccessor;
    class URLBytesCounter extends CLTBase
    {
    
        @Option(required= true)
        List<String> sites;
        
        @Override
        protected void realMain(OptionAccessor arg0) {
            sites.collectEntries{url->
                info("Working on ${url}")
                [url, new URL(url).text.length()]
            }.each{
                println "${it.value} bytes in ${it.key}"
            }
        }
        
        public static void main(String [] args) {
                CLTBase._main(new URLBytesCounter(), args);
        }
}
```

Example of using SQL query
```

    import org.himalay.persist.RDBMS
    import org.himalay.persist.Table
    public static void main(String [] args){
        String url ='https://data.ny.gov/api/views/sjc6-ftj4/rows.csv?accessType=DOWNLOAD';
        File file = File.createTempFile("bbb", ".csv")
        file.deleteOnExit();
        file.text = new URL(url).text
        RDBMS rdbms = RDBMS.h2Mem('bb',false);
        rdbms.importCSV(file, 'broadband');
        String table = 'broadband';
        int limit = 20;
        rdbms.h2AutoColumnsType(table, limit)
        Table tbl = rdbms.toTable('SELECT sum("# Cable Providers") providers, County FROM BROADBAND group by county order by providers');
        tbl.csvDelim = '|';
        println tbl.toCSV();
    }   
```