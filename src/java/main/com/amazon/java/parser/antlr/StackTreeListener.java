package com.amazon.java.parser.antlr;

public abstract class StackTreeListener extends JavaBaseListener {

    protected final StackTreeWalker walker;

    protected StackTreeListener(StackTreeWalker walker) {
        this.walker = walker;
    }

    public void resume() {

    }

}
