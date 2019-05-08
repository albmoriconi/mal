import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class MalCompiler {
    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);

        MalLexer lexer = new MalLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalParser parser = new MalParser(tokens);
        ParseTree tree = parser.uProgram(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        MalTranslator translator = new MalTranslator();
        walker.walk(translator, tree); // initiate walk of tree with listener

        for (MalTranslator.TranslatedInstruction ti : translator.translatedProgram) {
            System.out.format("Addr: %-5s NextAddr: %-13s uInstr: %s%n",
                    ti.address, ti.nextAddress, ti.instruction);
        }
    }
}
