package com.nikit;

/**
 * В моём решении автомат - это граф, поэтому состояния в нём - ноды
 */
class Node implements Cloneable{
    boolean fin;
    String name;
    MyList<Integer> idx;

    public Node(boolean fin, String name) {
        this.fin = fin;
        this.name = name;
        idx = new MyList<>();
    }
    public Node(String name){
        this.fin = false;
        this.name = name;
        idx = new MyList<>();
    }

    @Override
    public String toString() {
        return "\n\tNode={fin " + fin + ", name=" + name + ", idx=" + idx + "}";
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
