package org.himalay.commandline;

import groovy.json.JsonBuilder
import groovy.util.OptionAccessor

public class CredTool extends CLTBaseQuiet{

	@Option(required = true, description= 'The passphrase')
	String passphrase;

	@Option(description= 'The old passphrase')
	String oldPassphrase;

	@Option(required = true, description= 'The JSON file with credentials')
	File credFile;
	
	@Option(description= 'The secret', extend = true)
	String secret;
	
	@Option(description= 'The name of the secret')
	String name;
	
	@Option(required=true, regex='set|reencrypt|dump')
	String action
	

	public void preMain(String[] args){
		//credJsonFile = new File(this.credFile.path)
		
	}
	
	public String getSecret(String name){
		def json = new Util().getJsonConf(credFile.path)
		String base64 = json[name]
		if (base64 == null){
			warn ("No sercret with name ${name}")
		}else{
			return Util.decrypt(this.passphrase,base64);
		}
	}
	
	public void setSecret(String name, String secret){
		def json = new Util().getJsonConf(credFile.path)
		String base64 = Util.encrypt(this.passphrase,secret);
		json[name] = base64;
		credFile.text = new JsonBuilder(json).toPrettyString();
	}
	
	@Override
	protected void realMain(OptionAccessor options) {
		if ( action == 'set'){
			addSecret();
		}else if ( action == 'reencrypt'){
			reEncrypt()
		}else if ( action == 'dump'){
			dump()
		}
	}
	
	private void dump(){
		def json = new Util().getJsonConf(credFile.path)
		json.keySet().each{
			String base64 = json[it];
			String clear = Util.decrypt(this.passphrase,base64);
			//clear = new String(clear.decodeBase64());
			info "${name}=${clear}"
		}
	}
	
	private void addSecret(){
		setSecret(this.name, this.secret)
	}
	
	private void reEncrypt(){
		def json = new Util().getJsonConf(credFile.path)
		def newJson = [:]
		json.keySet().each{
			String base64 = json[it];
			String clear = Util.decrypt(this.oldPassphrase,base64);
			newJson[it]  = Util.encrypt(this.passphrase, clear);
		}
		credFile.text = new JsonBuilder(newJson).toPrettyString();
	}
	
	public static void main(String []args){
		CLTBase._main(new CredTool(), args);
	}
}
