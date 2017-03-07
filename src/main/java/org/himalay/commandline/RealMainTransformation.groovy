package org.himalay.commandline
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.ASTNode

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RealMainTransformation implements ASTTransformation {

	void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
		def (annotation, klass) = astNodes
		def cll = new AstBuilder().buildFromString(
				"""
		import org.himalay.commandline.CLTBase
        class ${klass.name} {
			def args;
            public static void main(String [] args0) {
				${klass.name} instance = new ${klass.name}()
				instance.args = args0
                CLTBase._main(instance, args0)
            }
        }
        """
				)

		def main = cll[1].methods[0].find { it.name == 'main' }
		def args  = cll[1].fields.find { it.name == 'args' }
		klass.addMethod(main)
		klass.addField(args)
	}
}

