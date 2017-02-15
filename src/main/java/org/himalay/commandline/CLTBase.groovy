package org.himalay.commandline

import org.apache.commons.cli.Option

import groovy.lang.Closure
import groovy.util.OptionAccessor

import java.io.File
import java.lang.reflect.Field
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Map

abstract class CLTBase extends Configurable{
	static String CREDENTIALS_FILE = System.getProperty("user.home")+ "/"+ $/etc/credentials.json/$ ;//new File()
	
	public static void _main(CLTBase instance, String [] args)
	{
		OptionAccessor opt = instance.parseArgs(args);
		if ( opt == null)
		{
			System.exit(-1)
		}else if (instance.verifyOptions(opt) == 0)
		{
			instance.realMain(opt);
		}else{
			instance.optionsVerificationFailed(opt, args);
		}
		
		instance.postMain()
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
	 * This function will be executed after the realMain
	 */
	protected void postMain()
	{
		
	}
	
	
	protected int verifyOptions(OptionAccessor options)
	{
		return 0;
	}
	
	CLTBase()
	{
		
	}
	
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing
	 * @return CliBuilder to be used in fillCli
	 */
	protected CliBuilder getCliBuilder()
	{
		return new CliBuilder(usage:"groovy ${this.class.name} <options>", header:'Options:', width: 100);
	}
	
	/**
	 * The default implementation adds options for any variable having Arg annotation.
	 * @param args
	 * @return
	 */
		protected OptionAccessor parseArgs(String... args) {
		CliBuilder cli = getCliBuilder();
		fillCli(cli);
		OptionAccessor options  = cli.parse(args);
		assignArgs(options)
		return options
	}
	
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing
	 *
	 */
	protected void fillCli(CliBuilder cli)
	{
		this.class.declaredFields.each{Field aField->
			aField.isAnnotationPresent(Arg.class)
			Arg arg = aField.getAnnotation(Arg.class)
			
			if ( arg != null )
			{				// Add to cli
				String longOpt = arg.longOpt     ()==""? aField.name    :arg.longOpt     ()
				if ( longOpt.endsWith("s"))
				{
					longOpt = longOpt.substring(0, longOpt.length() -1)
				}
				String shortOpt = arg.shortOpt()
				String argName  = arg.argName     ()==""? longOpt:arg.argName     ()
				debug("Adding ${longOpt}, argName =${argName}, numberOfArgs=${arg.numberOfArgs()}")
				cli."${arg.shortOpt()}"(
					longOpt     : longOpt,
					argName     : argName,
					required    : arg.required    ()                                    ,
					optionalArg : arg.optionalArg ()                                    ,
					args        : arg.numberOfArgs(),
					arg.description ()==""? aField.name:arg.description (),
				)
			}
		}
	}
	
	/**
	 * Can be optionally overwritten by derived classes to customize CLI parsing/assignment
	 * @param options
	 */
	protected void assignArgs(OptionAccessor options)
	{
		if ( options != null){
			this.class.declaredFields.each{Field aField->
				aField.isAnnotationPresent(Arg.class)
				Arg arg = aField.getAnnotation(Arg.class)
				
				if ( arg != null )
				{
					
					String name = aField.name.substring(0,1).toUpperCase()+ aField.name.substring(1)
					Object val = options."${aField.name}"
					if (
						val.class.name != 'java.lang.Boolean' || 	( aField.type.name =='java.lang.Boolean' && val.class.name == 'java.lang.Boolean' )
					)
					{
						if ( val != null){
							this."set${name}"(val)
							debug("Assigned ${aField.name}=${val}")
						}
					}
				}
			}
		}

	}
	
	
	/**
	 * 
	 * @param options
	 */
	protected abstract void realMain(OptionAccessor options);

	/**
	 *
	 * @param cmd The command to execute
	 * @param envMap map of environment variables
	 * @param workFolder The work folder
	 * @param ioClosure A that take three arguments: procOutStrm, procInStr, procErrStr. If this passed as null then err stream and out stream are read and returned as err, out
	 * @return
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
			return [out: sout , err: serr, , exitValue: proc.exitValue() ]
		}else{ // This portion does not work yet
			Process proc = null
			Thread th = Thread.start {
				proc = cmd.execute(env,new File(workFolder))
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
	 * @return
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
	 * Hashes a file FileObject
	 * @param fo
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
	
	public def getCredentials(String server, String credentialsFile=CREDENTIALS_FILE)
	{
		def cred = new groovy.json.JsonSlurper().parse(new File(credentialsFile))[server]
		return cred
	}

}

