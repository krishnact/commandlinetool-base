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
	    <version>0.4.3</version>
</dependency>
```
See jitpack to learn how to used in other build tools.
https://jitpack.io/#krishnact/commandlinetool-base/0.4.3

Example: Writing a simple URL Size counter, in Groovy.
```
@Grapes([
        @GrabResolver(name='jitpack', root='https://jitpack.io'),
        @Grab(group='com.github.krishnact', module='commandlinetool-base', version='0.4.3'),
        @Grab(group='org.slf4j', module='slf4j-log4j12', version='1.7.7')
])
import org.himalay.commandline.Option;
import org.himalay.commandline.CLTBase;

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
