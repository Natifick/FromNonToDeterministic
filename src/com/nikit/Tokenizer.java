package com.nikit;

import java.util.LinkedList;

/**
 * Токенизатор разбирает "предложение" на токены
 * Попутно определяя, являются ли они терминальными
 */
class Tokenizer{
    public static LinkedList<Token> tokenize(String input){
        StringBuilder t = new StringBuilder();
        LinkedList<Token> output = new LinkedList<>();
        boolean terminal = false;
        for (char c: input.toCharArray()){
            // Общие правила к именам - только латиница, цифры и '_'
            boolean b = (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_');
            if (t.length() != 0) {
                if (b) {
                    if (terminal) {
                        output.add(new Token(true, t.toString()));
                        terminal = false;
                        t = new StringBuilder();
                    }
                } else {
                    if (!terminal) {
                        output.add(new Token(t.toString()));
                        terminal = true;
                        t = new StringBuilder();
                    }
                }
            }
            if (c != ' '){
                terminal = !(b);
                t.append(c);
            }
            else{
                if (t.length()!=0){
                    output.add(new Token(terminal, t.toString()));
                    t = new StringBuilder();
                }
            }
        }
        if (t.length() != 0){
            output.add(new Token(terminal, t.toString()));
        }
        System.out.println("Токены для строки:\t" + output);
        return output;
    }
}
