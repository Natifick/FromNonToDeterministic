package com.nikit;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Токенизатор разбирает "предложение" на токены
 * Попутно определяя, являются ли они терминальными
 */
class Tokenizer{
    private static int count_parameters(String name){
        return switch (name) {
            case "-", "+", "++", "--" -> 0;
            case "!" -> 1;
            default -> 2;
        };
    }
    // b=0; if (2==3) a=1; else {a=2; b=2;}
    // TODO todo todo-todo-todo-todo-todoooooo todododo
    public static LinkedList<String> terminals = new LinkedList<>();
    public static LinkedList<String> words = new LinkedList<>();
    public static boolean verbose = false;
    // Для проверки чисел и строк будем использовать регулярные выражения
    static Pattern number = Pattern.compile("[+\\-]?(\\d+\\.\\d+)|(\\d+)");
    static Pattern string = Pattern.compile("(\"[^\"]+\")|('[^']+')");
    public static LinkedList<Token> tokenize(String input){
        System.out.println("terminals: " + terminals);
        System.out.println("words: " + words);
        input = input.trim(); // пробелы - игнорируем
        // Результат будет записан в output
        LinkedList<Token> output = new LinkedList<>();
        if (Tokenizer.verbose){
            System.out.println("Строка: " + input);
        }
        String temp;
        Matcher mat;
        while(input.length()!=0){
            // Сначала ищем самый близкий терминал к началу строки
            temp = "";
            for (String i: terminals){
                // Если мы или ещё не встречали символ, или встретили, но длина оказалась больше (выгоднее брать терминал большей длины)
                if ((temp.length()==0 || input.indexOf(i)<input.indexOf(temp) ||
                        input.indexOf(i)==input.indexOf(temp) && i.length()>temp.length()) && input.contains(i)){
                    // Можно было бы сразу заносить в стек, но нужно решать проблему с * и **
                    temp = i;
                }
            }
            for (String i: words){
                // Если мы или ещё не встречали символ, или встретили, но длина оказалась больше (выгоднее брать терминал большей длины)
                // TODO todo todo-todo-todo-todo-todoooooo todododo
                if ((temp.length()==0 || input.indexOf(i)==0 && (input.contains(" ") && input.indexOf(i)+i.length()<input.indexOf(" ") ||
                        !input.contains(" ") && input.indexOf(i)+i.length()<input.indexOf(temp) && terminals.contains(temp))) && input.contains(i)){
                    // Можно было бы сразу заносить в стек, но нужно решать проблему с * и **
                    temp = i;
                }
            }
            // Также проверим цифру и строку
            mat = string.matcher(input);
            if (mat.find() && mat.start()<input.indexOf(temp)){
                temp = input.substring(0, mat.end());
            }
            mat = number.matcher(input);
            if (mat.find() && mat.start()<input.indexOf(temp)){
                temp = input.substring(0, mat.end());
            }
            // Если в результате токен не достаточно близок к началу строки,
            // то, вероятно, это нераспознанная переменная, а она нетерминал
            if (input.indexOf(temp)==0 && !temp.equals("")){
                output.add(new Token(terminals.contains(temp) || words.contains(temp), input.substring(0, temp.length())));
                input = input.substring(temp.length());
                input = input.trim();
                System.out.println("1" + temp);
            }
            else if (!temp.equals("") && input.contains(" ")){
                output.add(new Token(input.substring(0, Math.min(!input.contains(" ") ?input.length():input.indexOf(" "), input.indexOf(temp)))));
                input = input.substring(Math.min(!input.contains(" ") ?input.length():input.indexOf(" "), input.indexOf(temp)));
                input = input.trim();
                System.out.println("2" + temp);
            }
            else if (!temp.equals("")){
                output.add(new Token(input.substring(0, input.indexOf(temp))));
                input = input.substring(input.indexOf(temp));
                input = input.trim();
                System.out.println("3" + temp);
            }
            else if (input.contains(" ")){
                output.add(new Token(input.substring(0, input.indexOf(" "))));
                input = input.substring(input.indexOf(" "));
                input = input.trim();
                System.out.println("4" + temp);
            }
            else {
                output.add(new Token(input));
                break;
            }
        }
        // Определяем количество операндов для операторов (чтобы выявить унарные)
        int cnt;
        for (int i=0;i<output.size();i++){
            if (output.get(i).terminal){
                if (i==0 || i==output.size()-1){
                    output.get(i).set_params(1);
                }
                else{
                    // Считаем операнды для числа
                    // Отдельная функция считает в зависимости от имени
                    output.get(i).set_params(count_parameters(output.get(i).name));
                    // Если есть проблемы с пониманием (унарный или бинарный минус), то решаем в зависимости от следующего символа
                    if (output.get(i).get_params()==0){
                        output.get(i).set_params(1+(!output.get(i+1).terminal || output.get(i+1).name.equals("(")?1:0));
                    }
                    output.get(i).leftAssoc = output.get(i+1).terminal || output.get(i + 1).name.equals("(");
                }
            }
            else{
                // Если это функция, то ставим 0
                output.get(i).set_params(0);
            }
        }
        System.out.println("Токены для строки:\t" + output);
        return output;
    }
}