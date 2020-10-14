package com.nikit;

import java.util.LinkedList;

/**
 * В моём решении используется автомат как граф, поэтому есть рёбра (переходы)
 */
class Transition{
    String label = "";
    Node from;
    LinkedList<Node> to;
    public Transition(String label, Node from, Node to){
        this.label = label;
        this.from = from;
        this.to = new LinkedList<>();
        this.to.add(to);
    }

    public void add(LinkedList<Node> to){
        this.to.addAll(to);
    }

    @Override
    public String toString() {
        return "\n\tlabel:" + label + "\tfrom " + from + "\tto " + to;
    }
}