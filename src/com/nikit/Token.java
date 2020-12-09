package com.nikit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

/**
 * Когда разбираем синтаксис, используем токены
 * Каждый токен при этом или терминальный (+,-,*...)
 * Или не терминальный (имена переменных)
 * Каждому входному токену присваивается тип данных
 */
class Token{
    enum Type{
        INT("int"), FLOAT("float"), BOOL("bool"), NONE("none"), STRING("string");
        private String name;
        Type(String name){
            this.name = name;
        }
        String get_type(){
            return this.name;
        }
    }
    Type type = Type.NONE;
    boolean terminal = false;
    boolean leftAssoc;
    String name;
    Type autotype = Type.NONE; // тип, который принимает переменная по умолчанию, если None, то не пытаемся присвоить
    final static Pattern FLOAT = Pattern.compile("[+\\-]?\\d+\\.\\d+");
    final static Pattern INT = Pattern.compile("[+\\-]?\\d+");
    final static Pattern BOOL = Pattern.compile("(true)|(false)");
    final static Pattern STRING = Pattern.compile("(\"[^\"]+\")|('[^']+')");
    private int count_of_parameters = 0;
    boolean equals(Token another){
        if (type == another.type){
            if (type == Type.STRING){
                return name.substring(1, name.length()-1).equals(another.name.substring(1, another.name.length()-1));
            }
            else{
                return this.name.equals(another.name);
            }
        }
        else if (type == Type.STRING || another.type == Type.STRING){
            return false;
        }
        else{
            return this.toFLOAT().name.equals(another.toFLOAT().name);
        }
    }
    /** Эту функцию будем использовать только для определения больше/меньше */
    double compare(Token another){
        if (type == another.type){
            if (type == Type.STRING){
                return 0;
            }
            else{
                return parseDouble(this.toFLOAT().name) - parseDouble(another.toFLOAT().name);
            }
        }
        else if (type == Type.STRING || another.type == Type.STRING){
            return 0;
        }
        else{
            return parseDouble(this.toFLOAT().name) - parseDouble(another.toFLOAT().name);
        }
    }
    public Token(boolean terminal, String name) {
        this.terminal = terminal;
        this.name = name;
        leftAssoc = switch (name) {
            // Если уж возник вопрос, то наверняка операторы ++ и -- уже правоассоциативные
            case "=", "*=", "+=", "-=", "/=", "%=" -> false;
            default -> true;
        };
        if (FLOAT.matcher(name).matches()){
            this.type = Type.FLOAT;
        }
        else if (INT.matcher(name).matches()){
            this.type = Type.INT;
        }
        else if (BOOL.matcher(name).matches()){
            this.type = Type.BOOL;
        }
        else if (STRING.matcher(name).matches()){
            this.type = Type.STRING;
        }
    }
    public Token(boolean terminal, String name, int cnt) {
        this.terminal = terminal;
        this.name = name;
        this.count_of_parameters = cnt;
        leftAssoc = switch (name) {
            // Если уж возник вопрос, то наверняка операторы ++ и -- уже правоассоциативные
            case "=", "*=", "+=", "-=", "/=", "%=" -> false;
            default -> true;
        };
        if (FLOAT.matcher(name).matches()){
            this.type = Type.FLOAT;
        }
        else if (INT.matcher(name).matches()){
            this.type = Type.INT;
        }
        else if (BOOL.matcher(name).matches()){
            this.type = Type.BOOL;
        }
        else if (STRING.matcher(name).matches()){
            this.type = Type.STRING;
        }
    }
    public Token(String name){
        this.name = name;
        leftAssoc = switch (name) {
            // Если уж возник вопрос, то наверняка операторы ++ и -- уже правоассоциативные
            case "=", "*=", "+=", "-=", "/=", "%=" -> false;
            default -> true;
        };
        if (FLOAT.matcher(name).matches()){
            this.type = Type.FLOAT;
        }
        else if (INT.matcher(name).matches()){
            this.type = Type.INT;
        }
        else if (BOOL.matcher(name).matches()){
            this.type = Type.BOOL;
        }
        else if (STRING.matcher(name).matches()){
            this.type = Type.STRING;
        }
    }
    public Token toINT(){
        switch(type){
            case BOOL:
                return new Token(name.equals("true") ?"1":"0");
            case INT:
                return this;
            case FLOAT:
                return new Token(name.substring(0, name.contains(".")?name.indexOf("."):name.length()));
        }
        return null;
    }
    public Token toFLOAT(){
        switch (type){
            case BOOL:
                return new Token(name.equals("true") ?"1.0":"0.0");
            case INT:
                return new Token(name+".0");
            case FLOAT:
                return this;
        }
        return null;
    }
    public Token toBOOL(){
        switch (type){
            case BOOL:
                return this;
            case INT:
                return new Token(parseInt(name)>0?"true":"false");
            case FLOAT:
                return new Token(parseDouble(name)>0?"true":"false");
        }
        return null;
    }
    public void set_params(int cnt){
        this.count_of_parameters = cnt;
    }
    public int get_params(){
        return count_of_parameters;
    }
    @Override
    public String toString() {
        return "(name:'" + name + "'," + (terminal?"":"не") +"терминальный, type="+type+", params: " + count_of_parameters + ")";
    }
}

class Array extends Token{
    LinkedList<Integer> params = new LinkedList<>();
    Token[] values;
    int count = 0;
    public Array(Token t){
        super(t.name);
        this.leftAssoc = t.leftAssoc;
        this.terminal = t.terminal;
        this.type = Type.NONE;
    }
    public void set_size(Token size){
        values = new Token[parseInt(size.toINT().name)];
        for (int i=0;i<parseInt(size.toINT().name);i++){
            values[i] = new Token("NULL");
        }
    }
    public Token get_value(Token position){
        return values[parseInt(position.toINT().name)];
    }
    public void add_val(Token val){
        params.addLast(parseInt(val.toINT().name));
        count = params.size()-1;
    }
    public Token pop(){
        if (count <= 0){
            count = params.size()-1;
        }
        return new Token(valueOf(params.get(--count)));
    }

    public String printArr() {
        StringBuilder out = new StringBuilder();
        LinkedList<Integer> arr = new LinkedList<>(params);
        for (int i=0;i<arr.size();i++){
            arr.set(i, 0);
        }
        out.append("[".repeat(params.size()));
        int closed;
        for (int i=0;i<values.length;i++){
            out.append(" ").append(values[i].name);
            arr.set(0, arr.get(0)+1);
            closed = 0;
            if (i!=values.length-1){
                for (int j=0;j<params.size()-1;j++){
                    if (arr.get(j)%params.get(j)==0){
                        arr.set(j+1, arr.get(j+1)+1);
                        out.append("]");
                        closed++;
                        if (closed>=params.size()/2){
                            out.append("\n");
                        }
                    }
                    else{
                        out.append(",");
                    }
                }
                for (int j=0;j<params.size()-1;j++){
                    if (arr.get(j)%params.get(j)==0){
                        arr.set(j+1, arr.get(j+1)+1);
                        out.append("[");
                    }
                }
            }
        }
        out.append("]".repeat(params.size()));
        return out.toString();
    }

    @Override
    public String toString() {
        return "Array{" +
                "params=" + params +
                ", values=" + Arrays.toString(values) +
                ", count=" + count +
                '}';
    }
}
