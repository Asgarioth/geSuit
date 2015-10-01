package net.cubespace.geSuit.core.commands;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

class ParseNode {
    private List<ParseNode> children;
    private ParseNode parent;
    private int variant;
    private int argumentIndex;
    private int inputIndex;
    private Function<String, ?> transformer;
    private boolean isTerminal;
    private boolean isVarArgs;
    
    private ParseNode(int variant, int argumentIndex, int inputIndex, Function<String, ?> transformer, boolean varArgs) {
        this.variant = variant;
        this.argumentIndex = argumentIndex;
        this.inputIndex = inputIndex;
        this.transformer = transformer;
        this.isVarArgs = varArgs;
        
        children = Lists.newArrayList();
    }
    
    private ParseNode(int variant, int argumentIndex, int inputIndex) {
        this.variant = variant;
        this.argumentIndex = argumentIndex;
        this.inputIndex = inputIndex;
        isTerminal = true;
    }
    
    public static ParseNode newTerminalNode(int variant, int argumentIndex, int inputIndex) {
        return new ParseNode(variant, argumentIndex, inputIndex);
    }
    
    public static ParseNode newTransformNode(int variant, int argumentIndex, int inputIndex, Function<String, ?> transformer, boolean varArgs) {
        return new ParseNode(variant, argumentIndex, inputIndex, transformer, varArgs);
    }
    
    public static ParseNode newRootNode() {
        return new ParseNode(-1, -1, -1, null, false);
    }
    
    public void addChild(ParseNode node) {
        Preconditions.checkArgument(node.parent == null);
        
        int score = node.getScore();
        boolean inserted = false;
        
        // Order as normal, varargs, terminal
        for (int i = 0; i < children.size(); ++i) {
            int otherScore = children.get(i).getScore();
            if (score < otherScore) {
                children.add(i, node);
                inserted = true;
                break;
            }
        }
        
        if (!inserted) {
            children.add(node);
        }
        
        node.parent = this;
    }
    
    public int getDepth() {
        int depth = 0;
        ParseNode node = parent;
        while (node != null) {
            ++depth;
            node = node.parent;
        }
        
        return depth;
    }
    
    private int getScore() {
        if (isTerminal) {
            return 2;
        } else if (isVarArgs) {
            return 1;
        } else {
            return 0;
        }
    }
    
    public List<ParseNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public ParseNode getParent() {
        return parent;
    }
    
    public boolean isTerminal() {
        return isTerminal;
    }
    
    public int getVariant() {
        return variant;
    }
    
    public boolean isVarArgs() {
        return isVarArgs;
    }
    
    public int getArgumentIndex() {
        return argumentIndex;
    }
    
    public int getInputIndex() {
        return inputIndex;
    }
    
    public Object parse(String value) {
        return transformer.apply(value);
    }
    
    public Function<String, ?> getTransformer() {
        return transformer;
    }
    
    public String getDebugName() {
        String text;
        if (isTerminal) {
            text = "*term*";
        } else {
            if (transformer != null) {
                try {
                    // Does it have a toString method
                    transformer.getClass().getMethod("toString");
                    text = transformer.toString();
                } catch (Throwable e) {
                    if (transformer.getClass().isAnonymousClass()) {
                        text = transformer.toString();
                    } else {
                        text = transformer.getClass().getSimpleName();
                    }
                }
            } else {
                text = "*root*";
            }
            
            if (isVarArgs) {
                text += "...";
            }
        }
        
        return String.format("%s varargs: %s arg: %d input: %d var: %d", text, isVarArgs, argumentIndex, inputIndex, variant);
    }
    
    @Override
    public String toString() {
        return String.format("%s:%s", getDebugName(), children);
    }
    
    void debugPrint(PrintStream out, int depth) {
        String padding = Strings.repeat(" ", depth * 2);
        out.println(padding + getDebugName() + " var: " + variant);
        if (children != null) {
            for (ParseNode child : children) {
                child.debugPrint(out, depth+1);
            }
        }
    }
}