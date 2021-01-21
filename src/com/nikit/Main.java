package com.nikit;
////////////////////////////////////////
//Автор программы: Хорошавцев Никита  //
////////////////////////////////////////

import java.util.*;
import static java.lang.String.valueOf;

/*
Тестовые входные значения:
S
A
Q
over
a
b
T
over
S ::= aQ|bT
A ::= a|aS|bTT
Q ::= b|bS|aQQ
over


S
A
E
id
over
+
*
(
)
;
over
S ::= A;
A ::= E|+E|A+E|A*E
E ::= id|(A)
over
id+(id*id);

*/

/**
 * главный класс, который принимает на вход LR грамматику в форме Бекуса-Науэра
 * И составляет конечный автомат по ней
 */
class Main{
    static LinkedList<Node> nodes;
    static LinkedList<Transition> trans;
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите все нетерминальные символы, для завершения введите 'over'");
        LinkedList<String> names = new LinkedList<>();
        String name;
        // Записывем нетерминальные символы
        while(sc.hasNextLine()){
            name = sc.nextLine();
            if (!name.equals("over")) {
                names.add(name);
            }
            else{
                break;
            }
        }
        Tokenizer.nonterminals = names;
        names = new LinkedList<>();
        names.add(" ::= ");
        names.add("|");
        System.out.println("Введите все терминальные символы, для завершения введите 'over'");
        // Записывем терминальные символы
        while(sc.hasNextLine()){
            name = sc.nextLine();
            if (!name.equals("over")) {
                names.add(name);
            }
            else{
                break;
            }
        }
        Tokenizer.terminals = names;
        names = new LinkedList<>();
        System.out.println("Введите построчно LR грамматику в формате [нетерминал ::= строка]\nДля завершения введите over");
        LinkedList<LinkedList<Token>> tokens = new LinkedList<>(); // Храним для последующего развёртывания
        while (sc.hasNextLine()){
            String[] temp = sc.nextLine().split(" ::= ");
            if (temp[0].equals("over")){
                break;
            }
            if (!names.contains(temp[0])){
                names.add(temp[0]);
            }
            Token current = new Token(temp[0]); // текущий рассматриваемый токен с правой стороны предложения
            int first = 0;
            LinkedList<Token> tmp = Tokenizer.tokenize(temp[1]);
            for (int i=0;i<tmp.size();i++){
                if (tmp.get(i).name.equals("|")){
                    tokens.add(new LinkedList<>());
                    tokens.getLast().add(current);
                    tokens.getLast().addAll(tmp.subList(first, i));
                    System.out.println(tmp.subList(first, i));
                    first = i+1;
                }
                else{
                    if (!tmp.get(i).terminal && !names.contains(tmp.get(i).name)){ // Если ещё не встречали символ справа
                        names.add(tmp.get(i).name);
                    }
                }
            }
            if (first!=tmp.size()-1){
                tokens.add(new LinkedList<>());
                tokens.getLast().add(current);
                tokens.getLast().addAll(tmp.subList(first, tmp.size()));
                System.out.println(tmp.subList(first, tmp.size()));
            }
        }
        System.out.println(tokens);
        nodes = new LinkedList<>();
        trans = new LinkedList<>();
        for (int i=0;i<tokens.size();i++){
            for (int j=1;j<tokens.get(i).size();j++){
                // нашим лейблом будет имя текущего токена, переход из позиции с номером j в j+1
                if (j==tokens.get(i).size()-1){
                    trans.add(new Transition(tokens.get(i).get(j).name, new Node(valueOf(j-1) + "," + valueOf(i)), new Node(tokens.get(i).get(0).name + ","+(tokens.get(i).size()-1), valueOf(j) + "," + valueOf(i))));
                }
                else{
                    trans.add(new Transition(tokens.get(i).get(j).name, new Node(valueOf(j-1) + "," + valueOf(i)), new Node(valueOf(j) + "," + valueOf(i))));
                }
                if (!tokens.get(i).get(j).terminal){
                    for (int tok =0;tok<tokens.size();tok++){
                        // проходимся по всем "уравнениям"
                        // и если в правую часть можно попасть с помощью пустого перехода, то идём туда
                        if (tokens.get(tok).getFirst().name.equals(tokens.get(i).get(j).name) && tokens.get(tok) != tokens.get(i)){
                            // имена равны, то есть из S ::= *A; можно пустым переходом попасть в A ::= E;
                            // Если я верно понял, то мы всегда приходим в ноду (0,y), то есть первая координата 0
                            trans.add(new Transition("", new Node(valueOf(j-1) + "," + valueOf(i)), new Node("0," + valueOf(tok))));
                        }
                    }
                }
            }
        }

        // Заполняем список нод по списку переходов
        for (Transition t: trans){
            boolean In1 = false, In2 = false;
            for (Node n: nodes){
                if (n.name.equals(t.from.name)){
                    In1 = true;
                }
                else if (n.name.equals(t.to.getFirst().name)){
                    In2 = true;
                }
                if (In1  && In2){
                    break;
                }
            }
            if (!In1){
                nodes.add(t.from);
            }
            if (!In2 && !t.from.name.equals(t.to.getFirst().name)){ // Если они одинаковые, то добавлять стоит лишь одно
                nodes.addAll(t.to);
            }
        }
        System.out.println(trans);
        System.out.println(nodes);
        FiniteAutomaton fa = new FiniteAutomaton(nodes, trans);
        fa.ToDetermined();
        while(true){
            System.out.println("Введите строку, которую необходимо разобрать по заданной грамматике:");
            fa.parseString(sc.nextLine(), tokens.getFirst().getFirst().name);
        }
    }
}