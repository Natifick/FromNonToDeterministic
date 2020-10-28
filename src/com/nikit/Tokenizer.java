package com.nikit;

import java.util.LinkedList;

/**
 * Токенизатор разбирает "предложение" на токены
 * Попутно определяя, являются ли они терминальными
 */
class Tokenizer{
    public static LinkedList<String> terminals;
    public static LinkedList<Token> tokenize(String input){
        StringBuilder t = new StringBuilder();
        LinkedList<Token> output = new LinkedList<>();
        int min, length;
        System.out.println("Строка: " + input);
        while(input.length()!=0){
            min = input.length();
            length = input.length();
            for (String i: terminals){
                if (input.contains(i) && input.indexOf(i)<min){
                    min = input.indexOf(i);
                    length = i.length();
                }
            }
            if (min==0){
                output.add(new Token(true, input.substring(0, length)));
            }
            else if (min!=input.length()){
                output.add(new Token(input.substring(0, min)));
                output.add(new Token(true, input.substring(min, min+length)));
            }
            else{
                output.add(new Token(input));
                break;
            }
            input = input.substring(min+length);
        }
        System.out.println("Токены для строки:\t" + output);
        return output;
    }
}
