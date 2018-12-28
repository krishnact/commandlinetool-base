package org.himalay.commandline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
//import java.util.Map;
//import javax.management.Attribute;
//import javax.management.AttributeList;
//import javax.management.AttributeNotFoundException;
//import javax.management.DynamicMBean;
//import javax.management.InvalidAttributeValueException;
//import javax.management.MBeanException;
//import javax.management.MBeanInfo;
//import javax.management.ReflectionException;
@Retention(value=RetentionPolicy.RUNTIME)
public @interface ExposeToJmx {
	String description      () default "";
}

//public void exposeToJmx2(boolean printClass = false){
//	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//	
//	Method[] methods = this.class.getMethods().findAll{Method method->
//		
//		boolean annotated = method.annotations.any{
//			it.annotationType() == ExposeToJmx.class
//			}
//		boolean getSet    =  ( ( method.name.startsWith('set') || method.name.startsWith('get')) && (method.getDeclaringClass() == this.class))
//		
//		return annotated || getSet
//	}
//	
//	def signatures = [] as ArrayList<String>
//	def impls = [] as ArrayList<String>
//	methods.each{aMethod->
//		String sig = aMethod.toString()
//		info sig
//		String[] parts = sig.split(/\(|\)/);
//		//if ( parts[0].split (" ")[-1].startsWith(this.class.name) ){
//			String decaringClassName = aMethod.getDeclaringClass().name
//			sig = parts[0].replaceFirst(" "+decaringClassName+"."," ")
//			sig = sig
//			String args = ""
//			String invokations = ""
//			if (parts.length > 1){
//				int idx = 0;
//				args = parts[1].split(",").collect {String argType->
//					"${argType} arg${idx++}"
//				}.join(',')
//				idx = 0;
//				invokations = parts[1].split(",").collect {String argType->
//					"arg${idx++}"
//				}.join(',')
//				
//			}
//			sig = sig +"("+args+")"
//			signatures << sig
//			String returnType = aMethod.getReturnType().toString();
//			String impl = sig +"{" + (returnType =='void' ? "" : "return ")+ "this.delegateObj.${aMethod.name}("+invokations + ")}";
//			impls << impl
//		//}
//		
//	}
//	
//	String interfaceDef = """
//		${this.class.package.toString()};
//		public interface ${this.class.simpleName}_JmxMBean{
//			${signatures.join(";\n")};
//		};
//	"""
//	
//	String impleDef = """
//	${this.class.package.toString()};
//	public class ${this.class.simpleName}_Jmx extends org.himalay.commandline.CLTBaseQuiet implements ${this.class.simpleName}_JmxMBean{
//		${this.class.simpleName}_Jmx(${this.class.name} deleg){
//			this.delegateObj = deleg
//		}
//		${this.class.simpleName} delegateObj
//        ${impls.join("\n")}
//	}
//	"""
//	if (printClass){
//		trace interfaceDef
//		trace impleDef
//	}
//	
//	GroovyClassLoader loader = new GroovyClassLoader( this.class.classLoader);
//	Class cls1 = loader.parseClass(interfaceDef)
//	Object cls2 = loader.parseClass(impleDef).newInstance(this)
//	//Object cls2 = new Opts_Jmx();
//	ObjectName name = new ObjectName("${this.class.name}:type=autombeans");
//	mbs.registerMBean(cls2, name);
//	debug "Registered to JMX"
//}
//
//
//
//}
//class MyDynamicMbean implements DynamicMBean{
//
//MBeanInfo beanInfo;
////Map getters = [:];
//Map  attributes= [:].withDefault {[:]};
//Map  operations = [:];
//MyDynamicMbean(CLTBase cltbase){
//	this.beanInfo = new MBeanInfo();
//	Method[] methods = cltbase.class.getMethods().findAll{Method aMethod->
//		ExposeToJmx annotattion = aMethod.getAnnotation(ExposeToJmx.class);
//		boolean annotated = (annotattion != null);
//		boolean getter    =  ( ( aMethod.name.startsWith('get')) && (aMethod.getDeclaringClass() == this.class) && aMethod.getParameterCount() == 0)
//		boolean setter    =  ( ( aMethod.name.startsWith('set') ) && (aMethod.getDeclaringClass() == this.class) && aMethod.getParameterCount() ==1 && aMethod.getReturnType() == void.class)
//		String sig = aMethod.toString()
//		
//		String[] parts = sig.split(/\(|\)/);
//		String decaringClassName = aMethod.getDeclaringClass().name
//		sig = parts[0].replaceFirst(" "+decaringClassName+"."," ")
//		sig = sig
//		String args = ""
//		String invokations = ""
//		if (parts.length > 1){
//			int idx = 0;
//			args = parts[1].split(",").collect {String argType->
//				"${argType} arg${idx++}"
//			}.join(',')
//			idx = 0;
//			invokations = parts[1].split(",").collect {String argType->
//				"arg${idx++}"
//			}.join(',')
//		}
//		sig = sig +"("+args+")"
//
//			
//		if ( annotated){
//			operations[sig] = [method: aMethod, sig: sig, desciption:annotattion?.description ]
//		}else if ( getter){
//			attributes[sig]['getter'] = aMethod
//			attributes[sig]['desciption'] = ''+annotattion?.description
//			attributes[sig]['name'] = aMethod.name.substring(3,4).toLowerCase() + aMethod.name.substring(4);
//		}else if ( setter){
//			attributes[sig]['setter'] = aMethod
//			attributes[sig]['desciption'] = ''+annotattion?.description
//			attributes[sig]['name'] = aMethod.name.substring(3,4).toLowerCase() + aMethod.name.substring(4);
//		}
//		return annotated || getter || setter
//	}
//	
//	// Create attribute info
//	beanInfo.attributes = attributes.values().collect{Map attrInfo->
//		MBeanAttributeInfo inf = new MBeanAttributeInfo(
//			attrInfo['name'],
//			attrInfo['desciption'],
//			attrInfo['getter'],
//			attrInfo['setter']
//			);
//		
//		return inf;
//	}
//	
//	beanInfo.operations = operations.keySet().collect{Map opInfo->
//		MBeanOperationInfo retVal = new MBeanOperationInfo(opInfo.description, opInfo.method)
//		return retVal
//	}
//	
//}
//@Override
//public Object getAttribute(String attribute)
//		throws AttributeNotFoundException, MBeanException, ReflectionException {
//	Method method = attributes[attribute].getter;
//	return method.invoke();
//}
//
//@Override
//public void setAttribute(Attribute attribute)
//throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
//	Method method = attributes[attribute].getter;
//	method.invoke(attribute.value);
//}
//
//@Override
//public AttributeList getAttributes(String[] attributes) {
//	AttributeList al = new AttributeList();
//	attributes.each{String attr ->
//		Attribute anAttr = new Attribute();
//		anAttr.name = attr
//		anAttr.value = getAttribute(attr)
//		al.add(anAttr);
//	}
//
//	return al
//}
//
//@Override
//public AttributeList setAttributes(AttributeList attributes) {
//	AttributeList al = new AttributeList();
//	attributes.each{Attribute attr ->
//		Attribute anAttr = new Attribute();
//		anAttr.name = attr.name
//		try{
//			anAttr.value = setAttribute(attr)
//			al.add(anAttr);
//		}catch(Exception ex){
//			
//		}
//		
//	}
//
//	return al
//
//}
//
//@Override
//public Object invoke(String actionName, Object[] params, String[] signature)
//		throws MBeanException, ReflectionException {
//	// TODO Auto-generated method stub
//	return null;
//}
//
//@Override
//public MBeanInfo getMBeanInfo() {
//	MBeanInfo retVal = new MBeanInfo();
//	
//	// TODO Auto-generated method stub
//	return retVal;
//}
//
//}