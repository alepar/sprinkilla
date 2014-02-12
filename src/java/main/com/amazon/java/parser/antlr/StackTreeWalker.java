package com.amazon.java.parser.antlr;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTreeWalker {

    private final Logger log = LoggerFactory.getLogger(StackTreeWalker.class);

    private final Deque<StackTreeListener> listeners = new ArrayDeque<>();

    private StackTreeListener currListener() {
        return listeners.getLast();
    }

    public void walk(ParseTree t) {
        if ( t instanceof ErrorNode) {
            currListener().visitErrorNode((ErrorNode) t);
            return;
        }
        else if ( t instanceof TerminalNode) {
            currListener().visitTerminal((TerminalNode) t);
            return;
        }
        RuleNode r = (RuleNode)t;
        enterRule(currListener(), r);
        int n = r.getChildCount();
        for (int i = 0; i<n; i++) {
            walk(r.getChild(i));
        }
        exitRule(currListener(), r);
    }

    public void pop() {
        final StackTreeListener from = listeners.removeLast();
        final StackTreeListener to = listeners.getLast();
        log.trace("switchback from {} to {}", from.getClass().getSimpleName(), to.getClass().getSimpleName());
        to.resume();
    }

    public void push(StackTreeListener listener) {
        final String from;
        if (!listeners.isEmpty()) {
            from = listeners.getLast().getClass().getSimpleName();
        } else {
            from = null;
        }
        log.trace("switch from {} to {}", from, listener.getClass().getSimpleName());
        listeners.addLast(listener);
    }

    protected void enterRule(StackTreeListener listener, RuleNode r) {
        final ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
        log.trace("enter {}: {}", ctx.getClass().getSimpleName(), ctx.getText());
        listener.enterEveryRule(ctx);
        ctx.enterRule(listener);
    }

    protected void exitRule(StackTreeListener listener, RuleNode r) {
        final ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
        log.trace("exit {}", ctx.getClass().getSimpleName());
        ctx.exitRule(listener);
        listener.exitEveryRule(ctx);
    }

}
