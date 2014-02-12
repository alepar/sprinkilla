package com.amazon.java.parser.antlr;

import java.io.IOException;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import com.amazon.java.ClassDefinition;
import com.amazon.java.parser.JavaSourceParser;

public class AntlrJavaSourceParser implements JavaSourceParser {

    @Override
    public ClassDefinition parse(Reader r) {
        try {
            final JavaLexer lexer = new JavaLexer(new ANTLRInputStream(r));
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final JavaParser parser = new JavaParser(tokens);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            final JavaParser.CompilationUnitContext tree = parser.compilationUnit();

            final StackTreeWalker walker = new StackTreeWalker();
            final SourceFileClassExtractor extractor = new SourceFileClassExtractor(walker);
            walker.push(extractor);
            walker.walk(tree);
            return new AntlrClassDefinition(extractor);
        } catch (IOException e) {
            throw new RuntimeException("could not read java source", e);
        }
    }

}
