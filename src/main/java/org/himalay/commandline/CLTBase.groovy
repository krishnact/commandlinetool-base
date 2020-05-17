package org.himalay.commandline


import groovy.lang.Closure

import java.io.File
import java.lang.management.ManagementFactory
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.file.FileSystems
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Map

import javax.management.MBeanServer
import javax.management.ObjectName

import org.slf4j.LoggerFactory
import org.slf4j.Logger

import groovy.cli.commons.CliBuilder;
import groovy.cli.commons.OptionAccessor;
class CLTBase implements AutoConfig, AutoLogger{
	public static Logger LOGGER = LoggerFactory.getLogger(this.class);
	static String CREDENTIALS_FILE = System.getProperty("user.home")+ "/"+ $/etc/credentials.json/$ ;
	protected CliBuilder cliBuilder_ = null
	protected OptionAccessor opt = null;
	volatile protected boolean running = false;
	WatchService ws = null
	public static void _main(CLTBase instance, String [] args)
	{
		OptionAccessor opt = instance.parseArgs(args);
		if ( opt == null)
		{
			System.exit(-1)
		}else if (instance.verifyOptions(opt) == 0)
		{
			instance.preMain(args);
			instance.running = true;
			instance.realMain(opt);
		}else{
			instance.optionsVerificationFailed(opt, args);
		}
		instance.stopThis();
		instance.postMain()
		if (System.getenv().get('DONT_ADD_SHUTDOWNHOOK') != null) {
			Runtime.addShutdownHook {
				instance.LOGGER.info('Program exiting.')
			}
		}
	}
	
	private void stopThis() {
		running = false;
		this.ws.close();
	}
	
	/**
	 * Overwrite this function in case you need to change the behavior. The default behavior is to 
	 * call System.exit(-1)
	 * @param opt
	 * @param args
	 */
	protected void optionsVerificationFailed(OptionAccessor opt, String []  args)
	{
		warn("Exiting because unable to verify args");
		System.exit(-2)
	}
	
	
	/**
	 * This function will be executed before the realMain
	 */
	protected void preMain(String [] args)
	{
		
	}
	
	/**
	 * This function will be executed after the realMain
	 */
	protected void postMain()
	{
		
	}
	
	
	protected int verifyOptions(OptionAccessor options)
	{
		return 0;
	}
	
	CLTBase(Map confData = null)
	{
		initAutoConf(true)
		if ( confData != null){
			this.addConf(confData);
		}
	}
	
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing
	 * @return CliBuilder to be used in fillCli
	 */
	protected CliBuilder getCliBuilder()
	{
		
		CliBuilder retVal = new CliBuilder(
			usage:"groovy ${this.class.name} <options> args", 
			header:'Options:', 
			width: 100,
			stopAtNonOption:false
			);
		this.cliBuilder_ = retVal
		return retVal
	}
	
	/**
	 * The default implementation adds options for any variable having Arg annotation.
	 * @param args
	 * @return OptionAccessor
	 */
		protected OptionAccessor parseArgs(String... args) {
		this.cliBuilder_ = getCliBuilder();
		fillCli(this,cliBuilder_);
		OptionAccessor options  = cliBuilder_.parse(args);
		Object inner = null;
		try{
			inner = options?.getCommandLine()
		}catch(Exception ex){
			
		}
		if ( options == null ) {// || inner == null){
			examples();
			options = null;
		}else{
			assignConfValues(this,getConf());
			if ( assignArgs(this,options,getConf()) == false){
				optionsVerificationFailed(options, args);
				options = null;
			}
		}
		return options
	}
	
	public static Map<String,String> getOptionName(Field aField) {
		Option arg = aField.getAnnotation(Option.class)
		String aField_type_name = aField.type.name
		String longOpt = arg.longOpt     ()==""? aField.name    :arg.longOpt     ()
		boolean isList = false;
		if ( longOpt.endsWith("s") && aField_type_name =='java.util.List')
		{
			longOpt = longOpt.substring(0, longOpt.length() -1);
			isList = true
		}
		String shortOpt = arg.shortOpt()
		String argName  = arg.argName     ()==""? longOpt:arg.argName     ()
		String optionName = argName;
		if ( isList) {
			optionName = optionName+'s';
		}
		return [argName:argName, longOpt: longOpt,isList : isList, optionName: optionName ];
	}
	
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing
	 *
	 */
	public static void fillCli(Object obj, CliBuilder cli)
	{
		int maxOptionLength = 0;
		obj.class.declaredFields.each{Field aField->
			//aField.isAnnotationPresent(Option.class)
			Option arg = aField.getAnnotation(Option.class)
			String aField_type_name = aField.type.name
			def fieldClass = aField.clazz
			if ( arg != null )
			{	// Add to cli
				Map<String,String> optAndArgName = getOptionName(aField);
				String longOpt = optAndArgName.longOpt  
				String argName  = optAndArgName.argName 
				
				LOGGER.debug("Adding ${longOpt}, argName =${argName}, numberOfArgs=${arg.numberOfOptions()}")
				String desc = arg.description ()==""? aField.name:arg.description ()
				if (desc[-1] != '.'){
					desc = desc +'.'
				}
				
				if (arg.required () ){
					LOGGER.trace "Adding required marker"
					desc = '*' + desc
				}else {
					desc = ' ' + desc
				}
				
				// If a regex is specified then add that to description.
				if (arg.regex() != '(.)*')
				{
					desc += " Possible values: ${arg.regex()}."
				}
				

				// See if there is a default value
				String properCaseName = aField.name.substring(0,1).toUpperCase()+ aField.name.substring(1)
				def currVal = obj."get${properCaseName}"()
				if ( currVal != null){
					desc = desc + " Default: ${currVal}"
				}
				String shortOptName = arg.shortOpt()
				cli."${shortOptName}"(
					longOpt     : longOpt,
					argName     : argName,
					required    : arg.required    ()                                    ,
					optionalArg : arg.optional ()                                    ,
					args        : arg.numberOfOptions(),
					desc,
				)
				if ( longOpt.length() > maxOptionLength){
					maxOptionLength = longOpt.length()
				}
			}
		}
		cli.setWidth(maxOptionLength +130);
	}
	
	

	/**
	 * Reads and assigns values from conf file to all the members that are annotated with Configure annotation
	 * @return
	 */
	public static boolean assignConfValues(Object object, Map conf) {
		boolean retval = true;
		object.class.declaredFields.each{Field aField->
			Configure confVal = aField.getAnnotation(Configure.class)
			if ( confVal != null )
			{

				String name = aField.name.substring(0,1).toUpperCase()+ aField.name.substring(1)
				Object val = null

				LOGGER.trace "Trying value from config for ${aField.name}"
				String key = confVal.key();
				if ( key == '') {
					key = aField.getName();
				}
				val = Util.getConfVal(conf,key);
				
				if ( val != null) {
					String valClassName = val?.class.name
					String aField_type_name = aField.type.name
					def fieldClass = aField.clazz
					if (
					valClassName!= 'java.lang.Boolean' ||
					( aField.type.name =='boolean' && valClassName == 'java.lang.Boolean' )
					)
					{
						if (!( val ==~ confVal.regex()))
						{
							String msg = "${name} does not match ${confVal.regex()}"
							LOGGER.warn(msg)
						}else if (val != null){
							if ( aField_type_name =="java.lang.String" ){
								object."set${name}"(val as String)
							}else if ( aField_type_name =="int"){
								object."set${name}"(val as Integer)
							}else if ( aField_type_name =="long"){
								object."set${name}"(val as Long)
							}else if ( aField_type_name =="short"){
								object."set${name}"(val as Short)
							}else if (aField_type_name =="byte"){
								object."set${name}"(val as Byte)
							}else if (aField_type_name =="char"){
								object."set${name}"(val as Character)
							}else if ( aField_type_name =="float"){
								object."set${name}"(val as Float)
							}else if (aField_type_name =="double"){
								object."set${name}"(val as Double)
							}else if (aField_type_name =="java.io.File"){
								File file = new File(val);
								if ( confVal.fileExistence() == 0){

								}else if ( confVal.fileExistence() == 1 && !file.exists()){
									LOGGER.warn ("File ${file} does not exit")
									retval = false
								}else if ( confVal.fileExistence() == 2 && !file.exists()){
									if ( confVal.isFolder()){
										file.mkdirs()
									}else{
										file.parentFile.mkdirs()
										file.text ="";
									}
								}
								object."set${name}"(file)
							}else{
								object."set${name}"(val)
							}

							LOGGER.debug("Assigned ${aField.name}=${val}")
						}
					}
				}else {
					LOGGER.trace "Using default value for ${aField.name}"
				}
			}
		}

		return retval
	}
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing/assignment
	 * @param options
	 */
	public static boolean assignArgs(Object obj, OptionAccessor options, def confMap)
	{
		boolean retval = false;
		if ( options != null){
			retval = true;
			obj.class.declaredFields.each{Field aField->
				//aField.isAnnotationPresent(Option.class)
				Option arg = aField.getAnnotation(Option.class)
				if ( arg != null )
				{
					
					String name = aField.name.substring(0,1).toUpperCase()+ aField.name.substring(1)
					//Object val = options.getProperty(aField.name)
					Map<String,String> optAndArgName = getOptionName(aField);
					String longOpt = optAndArgName.longOpt
					String argName  = optAndArgName.argName

					//Object val = options.getOptionValue(optAndArgName.fieldName);
					Object val = options.getProperty(optAndArgName.optionName);
					if (val == null){
						LOGGER.trace "Trying value from config for ${aField.name}"
						val = confMap[aField.name];
					}
					if ( val != null) {
					String valClassName = val?.class.name
					String aField_type_name = aField.type.name
					def fieldClass = aField.clazz
					if (
						 valClassName!= 'java.lang.Boolean' || 	
						 ( aField.type.name =='boolean' && valClassName == 'java.lang.Boolean' )
					)
					{
						if ( val == null)
						{
						}else if (!( val ==~ arg.regex()))
						{
								String msg = "${argName} does not match ${arg.regex()}"
								if (arg.required() == true)
								{
									throw new IllegalArgumentException(msg) 		
								}else{
									LOGGER.warn(msg)
								}
						}else if (val != null){
							if ( (val instanceof List<String> ) && arg.extend() == true)
							{
								List<String> listVal = [] as ArrayList<String>
								val.each{String aVal->
									if (aVal.startsWith("=")){
										//A file has been specified. Use contents from the file
										new File(aVal.substring(1)).eachLine{String aLine->
											listVal << aLine
										}
									}else{
										listVal << aVal
									}
								}
								val = listVal
								obj."set${name}"(val)
							}else if ( aField_type_name =="java.lang.String" && arg.extend() == true){
								if (val.startsWith("=")){
									//A file has been specified. Use contents from the file
									obj."set${name}"(new File(val.substring(1)).text)
								}else{
									obj."set${name}"(val as String)
								}
							}else if ( aField_type_name =="int"){
								obj."set${name}"(val as Integer)
							}else if ( aField_type_name =="long"){
								obj."set${name}"(val as Long)
							}else if ( aField_type_name =="short"){
								obj."set${name}"(val as Short)
							}else if (aField_type_name =="byte"){
								obj."set${name}"(val as Byte)
							}else if (aField_type_name =="char"){
								obj."set${name}"(val as Character)
							}else if ( aField_type_name =="float"){
								obj."set${name}"(val as Float)
							}else if (aField_type_name =="double"){
								obj."set${name}"(val as Double)
							}else if (aField_type_name =="java.io.File"){
								File file = new File(val);
								if ( arg.fileExistence() == 0){
									
								}else if ( arg.fileExistence() == 1 && !file.exists()){
									LOGGER.warn ("File ${file} does not exit")
									retval = false
								}else if ( arg.fileExistence() == 2 && !file.exists()){
									if ( arg.isFolder()){
										file.mkdirs()
									}else{
										file.parentFile.mkdirs()
										file.text ="";
									}
								}
								obj."set${name}"(file)
							}else{
								obj."set${name}"(val)
							}
							
							LOGGER.debug("Assigned ${aField.name}=${val}")
						}
					}
					}else {
						LOGGER.trace "Using default value for ${aField.name}"
					}
				}
			}
		}
		return retval
	}
	
	
	/**
	 *
	 *Example use of this method:<pre>
     *{@code
     * executeAnExternalCmd('ping -c 1000 127.0.0.1',System.getenv(),".",{OutputStream o, InputStream i,InputStream e->     
     * 	def threads = [                                                                                      
     * 		[i,System.out],                                                                                  
     * 		[e,System.err]].collect{pair->                                                                   
     * 			Thread.start{Thread th->                                                                     
     * 				org.apache.commons.io.IOUtils.copy(pair[0], pair[1])                                     
     * 			}                                                                                            
     * 		}                                                                                                
     * 	                                                                                                     
     * 	def inThread = Thread.start{                                                                         
     * 		org.apache.commons.io.IOUtils.copy(i,System.out)                                                 
     * 	}                                                                                                    
     * 	threads.each{                                                                                        
     * 		it.join()                                                                                        
     * 	}                                                                                                    
     * 	i.close()                                                                                            
     * 	inThread.join()                                                                                      
     * })                                                                                                     }</pre>
	 * @param cmd The command to execute
	 * @param envMap map of environment variables
	 * @param workFolder The work folder
	 * @param ioClosure A that take three arguments: procOutStrm, procInStr, procErrStr. If this passed as null then err stream and out stream are read and returned as err, out
	 * @return [out: sout , err: serr, exitValue: proc.exitValue()]
	 */
	public static Map<String,String> executeAnExternalCmd(String cmd, Map envMap, String workFolder, Closure ioClosure)
	{
		def sout = new StringBuilder(), serr = new StringBuilder()
		String[] env = envMap.collect{
			return it.key +"="+ it.value
		}
		if (ioClosure == null)
		{
			def proc = cmd.execute(env,new File(workFolder))
			proc.consumeProcessOutput(sout, serr)
			proc.waitFor()
			return [out: sout , err: serr, exitValue: proc.exitValue() ]
		}else{ // This portion does not work yet
			Process proc = null
			Thread th = Thread.start {
				proc = cmd.execute(env,new File(workFolder))
				proc.waitFor()
				proc.closeStreams()
			}
			Thread.sleep(500)
			ioClosure(proc.outputStream, proc.inputStream, proc.errorStream)
			th.join();
			
			return [out: sout , err: serr, exitValue: proc.exitValue()]
		}
		return null;
	}
	
	/**
	 * This has been left here as an example
	 * @param tarFile
	 */
	public static void extractTarFile(File tarFile)
	{
		String executable = /C:\cygwin\bin\tar.exe/
		def ret     = executeAnExternalCmd("${executable} -xzvf ${tarFile.name}",tarFile.parentFile.absolutePath)
		println ret.out
	}
	
	/**
	 *
	 * @param cmd The command to execute
	 * @param workFolder The work folder
	 * @return [out: sout , err: serr, exitValue: proc.exitValue()]
	 */
	public static Map<String,String> executeAnExternalCmd(String cmd, String workFolder )
	{
		if (workFolder==null)
		{
			workFolder = System.getProperty("user.dir")
		}
		return executeAnExternalCmd(cmd,System.getenv(),workFolder,null)
	}

	public static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return stringBuffer.toString();
	}

	/**
	 * Hashes a file 
	 * @param inputStream
	 * @param algorithm default "SHA-256"
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String hashAFile(InputStream inputStream, String algorithm="SHA-256") throws NoSuchAlgorithmException ,IOException  {
		
		MessageDigest digest = MessageDigest.getInstance(algorithm);

		byte[] bytesBuffer = new byte[1024];
		int bytesRead = -1;

		while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
			digest.update(bytesBuffer, 0, bytesRead);
		}

		byte[] hashedBytes = digest.digest();

		return convertByteArrayToHexString(hashedBytes);
	}
	
	/**
	 * Get credentials from JSON credentials file (default is ~/etc/credentials.json)
	 * @param key
	 * @return
	 */
	public def getCredentials(String key, String credentialsFile=CREDENTIALS_FILE)
	{
		def cred = new groovy.json.JsonSlurper().parse(new File(credentialsFile))[key]
		return cred
	}

	/**
	 * Makes template using groovy.text.SimpleTemplateEngine
	 * @param templateText Template. Example "Hello I am ${name}."
	 * @param bindings The bindings. Example [name: 'Peter']
	 * @return Realized value. Example: "Hello I am Peter"
	 */
	public static String makeTemplate(String templateText, def bindings)
	{
		def engine = new groovy.text.SimpleTemplateEngine()
		def template = engine.createTemplate(templateText).make(bindings)
		return  template.toString()
	}

	protected void realMain(OptionAccessor options){
		throw new RuntimeException("Unimplemented. Please implement.");
	}
	/**
	 * This method will be called after the usage is printed.
	 */
	public void examples(){
		
	}
	
	/**
	 * Exposes getter/setter and ExposeToJmx annotated methods to JMX.
	 * At runtime, the JMX methods can be expose as REST APIs using jolokia just by passing -javaagent=jolokia.jar
	 * @arg printClass if this is true then the JMX supporting classes will be printed as debug level
	 */
	public void exposeToJmx(boolean printClass = false){
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Method[] methods = this.class.getMethods().findAll{Method method->
			
			boolean annotated = method.annotations.any{
				it.annotationType() == ExposeToJmx.class
				}
			boolean getSet    =  ( ( method.name.startsWith('set') || method.name.startsWith('get')) && (method.getDeclaringClass() == this.class))
			
			return annotated || getSet
		}
		
		def signatures = [] as ArrayList<String>
		def impls = [] as ArrayList<String>
		methods.each{aMethod->
			String sig = aMethod.toString()
			info sig
			String[] parts = sig.split(/\(|\)/);
			//if ( parts[0].split (" ")[-1].startsWith(this.class.name) ){
				String decaringClassName = aMethod.getDeclaringClass().name 
				sig = parts[0].replaceFirst(" "+decaringClassName+"."," ")
				sig = sig
				String args = ""
				String invokations = ""
				if (parts.length > 1){
					int idx = 0;
					args = parts[1].split(",").collect {String argType->
						"${argType} arg${idx++}"
					}.join(',')
					idx = 0;
					invokations = parts[1].split(",").collect {String argType->
						"arg${idx++}"
					}.join(',')
					
				}
				sig = sig +"("+args+")"
				signatures << sig
				String returnType = aMethod.getReturnType().toString();
				String impl = sig +"{" + (returnType =='void' ? "" : "return ")+ "this.delegateObj.${aMethod.name}("+invokations + ")}";
				impls << impl
			//}
			
		}
		
		String interfaceDef = """
			${this.class.package.toString()};
			public interface ${this.class.simpleName}_JmxMBean{
				${signatures.join(";\n")};
			};
		"""
		
		String impleDef = """
		${this.class.package.toString()};
		public class ${this.class.simpleName}_Jmx extends org.himalay.commandline.CLTBaseQuiet implements ${this.class.simpleName}_JmxMBean{
			${this.class.simpleName}_Jmx(${this.class.name} deleg){
				this.delegateObj = deleg
			}
			${this.class.simpleName} delegateObj
            ${impls.join("\n")}
		}
		"""
		if (printClass){
			trace interfaceDef
			trace impleDef
		}
		
		GroovyClassLoader loader = new GroovyClassLoader( this.class.classLoader);
		Class cls1 = loader.parseClass(interfaceDef)
		Object cls2 = loader.parseClass(impleDef).newInstance(this)
		//Object cls2 = new Opts_Jmx();
		ObjectName name = new ObjectName("${this.class.name}:type=autombeans");
		mbs.registerMBean(cls2, name);
		debug "Registered to JMX"
	}
	
	/**
	 * Re read the config file
	 * @param confFilePath
	 */
	@ExposeToJmx
	public void refreshConf(String confFilePath){
		debug "Refreshing conf file"
		initAutoConf(true);
	}
	
	public void configRead(){
		debug "Config file read"
	}
	
	public void realodLoggerConfiguration() {
		_LOGGER_HELPER.reloadLogConfiguration();
	}
	
	public Thread watchForLoggerConfigurationChange() {
		final File logFile = new File(LOGGER_CONFIG_LOADER.loggerConfigFilePath);
		final Path path = logFile.getParentFile().toPath();
		info("Watching logger file ${logFile}")
		
		Thread retVal = null;
		ws = FileSystems.getDefault().newWatchService();
		ws.with { watchService ->
			final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			retVal = Thread.start{
				while (running) {
					WatchKey wk = null
					try{
					wk = watchService.take();
					}catch(Exception ex){
						if (running) {
							warn("Exception while processing ${logFile.getName()}", ex)
						}
					}
					if (wk == null) {
						break;
					}
					for (WatchEvent<?> event : wk.pollEvents()) {
						//we only register "ENTRY_MODIFY" so the context is always a Path.
						final Path changed = (Path) event.context();
						if (changed.getFileName().endsWith(logFile.getName())) {
							info("Reloading ${logFile.getName()}")
							try {
								realodLoggerConfiguration();
							}catch(Exception ex) {
								warn("Exception while processing ${logFile.getName()}", ex)
							}
						}
					}
				// reset the key
				boolean valid = wk.reset();
				if (!valid) {
					info("Key has been unregistered");
				}
			}
			}
		}
		
		return retVal;
	}
	
}

