package org.himalay.commandline
//
//import org.apache.commons.cli.Option
//
//import groovy.lang.Closure
//import groovy.util.OptionAccessor
//
//import java.io.File
//import java.lang.reflect.Field
//import java.security.MessageDigest
//import java.security.NoSuchAlgorithmException
//import java.util.Map
//
//trait CLTTrait extends CfgTrait {
//
//
//	/**
//	 * Overwrite this function in case you need to change the behavior. The default behavior is to
//	 * call System.exit(-1)
//	 * @param opt
//	 * @param args
//	 */
//	void optionsVerificationFailed(OptionAccessor opt, String []  args) {
//		warn("Exiting because unable to verify args");
//		System.exit(-1)
//	}
//
//
//	/**
//	 * This function will be executed after the realMain
//	 */
//	void postMain() {
//	}
//
//
//	int verifyOptions(OptionAccessor options) {
//		return 0;
//	}
//
//
//	/**
//	 * The default implementation adds options for any variable having Arg annotation.
//	 * @param args
//	 * @return
//	 */
//	OptionAccessor parseArgs(String... args) {
//		CliBuilder cli = new CliBuilder(usage:"groovy ${this.class.name} <options>", header:'Options:', width: 100)
//		this.class.declaredFields.each{Field aField->
//			aField.isAnnotationPresent(Arg.class)
//			Arg arg = aField.getAnnotation(Arg.class)
//
//			if ( arg != null ) {
//				// Add to cli
//				String longOpt = arg.longOpt     ()==""? aField.name    :arg.longOpt     ()
//				if ( longOpt.endsWith("s"))
//				{
//					longOpt = longOpt.substring(0, longOpt.length() -1)
//				}
//				String shortOpt = arg.shortOpt()
//				String argName  = arg.argName     ()==""? longOpt:arg.argName     ()
//				info("Adding ${longOpt}, argName =${argName}, numberOfArgs=${arg.numberOfArgs()}")
//				cli."${arg.shortOpt()}"(
//						longOpt     : longOpt,
//						argName     : argName,
//						required    : arg.required    ()                                    ,
//						optionalArg : arg.optionalArg ()                                    ,
//						args        : arg.numberOfArgs(),
//						arg.description ()==""? aField.name:arg.description (),
//						)
//			}
//		}
//		OptionAccessor options  = cli.parse(args);
//		if ( options != null){
//			this.class.declaredFields.each{Field aField->
//				aField.isAnnotationPresent(Arg.class)
//				Arg arg = aField.getAnnotation(Arg.class)
//
//				if ( arg != null )
//				{
//
//					String name = aField.name.substring(0,1).toUpperCase()+ aField.name.substring(1)
//					Object val = options."${aField.name}"
//					if (
//					val.class.name != 'java.lang.Boolean' || 	( aField.type.name =='java.lang.Boolean' && val.class.name == 'java.lang.Boolean' )
//					)
//					{
//						if ( val != null){
//							this."set${name}"(val)
//							info("Assigned ${aField.name}=${val}")
//						}
//					}
//				}
//			}
//		}
//
//		return options
//	}
//
//	/**
//	 *
//	 * @param options
//	 */
//	abstract void realMain(OptionAccessor options);
//
//
//}
//
