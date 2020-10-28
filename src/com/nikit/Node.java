package com.nikit;

import java.util.Enumeration;

/**
 * В моём решении автомат - это граф, поэтому состояния в нём - вершины графа
 */
class Node implements Cloneable{
    String flag;
    String name;
    MyList<Integer> idx;

    public Node(String fl, String name) {
        flag = fl;
        this.name = name;
        idx = new MyList<>();
    }
    public Node(String name){
        this.flag = "0";
        this.name = name;
        idx = new MyList<>();
    }

    @Override
    public String toString() {
        return "\n\tNode={fin " + flag + ", name=" + name + ", idx=" + idx + "}";
    }

    public Node clone(){
        try{
            Node n = (Node)super.clone();
            n.idx = new MyList<>();
            n.name = "";
            return n;
        }
        catch(CloneNotSupportedException ex){
            System.out.println("Что-то пошло не так при создании второй таблицы");
            return null;
        }
    }

    public Node copy(){
        try{
            Node n = (Node)super.clone();
            n.name = "";
            return n;
        }
        catch(CloneNotSupportedException ex){
            System.out.println("Что-то пошло не так при создании второй таблицы");
            return null;
        }
    }
}
