package com.nikit;

import java.util.LinkedList;

/**
 * Токенизатор разбирает "предложение" на токены
 * Попутно определяя, являются ли они терминальными
 */
class Tokenizer{
    public static LinkedList<String> nonterminals;
    public static LinkedList<String> terminals;
    public static LinkedList<Token> tokenize(String input){
        StringBuilder t = new StringBuilder();
        LinkedList<Token> output = new LinkedList<>();
        System.out.println("Строка: " + input);
        while(input.length()!=0){
            for (String i: nonterminals){
                if (input.indexOf(i)==0){
                    output.add(new Token(input.substring(0, i.length())));
                    input = input.substring(i.length());
                    break;
                }
            }
            for (String i: terminals){
                if (input.indexOf(i)==0){
                    output.add(new Token(true, input.substring(0, i.length())));
                    input = input.substring(i.length());
                    break;
                }
            }
        }
        System.out.println("Токены для строки:\t" + output);
        return output;
    }
}
