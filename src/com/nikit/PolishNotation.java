package com.nikit;

import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Перевод в польскую нотацию и вычисление выражения в ней
 */
public class PolishNotation {
    public static LinkedList<String> functions = new LinkedList<>(); // имена функций
    public static HashMap<String, Token> variables = new HashMap<>();
    public static boolean verbose=false;
    void Create(){
        functions.add("print");
        System.out.println("Вводите строки, для завершения введите 'over':");
        Scanner sc = new Scanner(System.in);
        System.out.print(">>> ");
        String input = sc.nextLine();
        StringBuilder fullLine = new StringBuilder();
        while (!input.equals("over")){
            fullLine.append(input);
            System.out.print(">>> ");
            input = sc.nextLine();
        }
        Stack<Token> polish = EquationToPolish(Tokenizer.tokenize(fullLine.toString()));
        System.out.println(polish);
        System.out.print("Строка в польской нотации: ");
        for (Token t: polish){
            if (!t.terminal && t.get_params()!=0){
                System.out.print(t.name + t.get_params() + " ");
            }
            else{
                System.out.print(t.name + " ");
            }
        }
        System.out.println();
        System.out.println("Введите что-нибудь чтобы продолжить");
        new Scanner(System.in).nextLine();
        parseLine(new LinkedList<>(polish));
    }
    /**
     * определение приоритетов операций
     */
    int priority(String name){
        // Внаглую скопируем из языка C
        // В случае, если мы не знаем, с чем имеем дело, то дадим 0, пусть будет важным
        return switch (name) {
            case "**" -> 1;
            case "++", "--" -> 2;
            case "*", "/", "%" -> 3;
            case "+", "-" -> 4;
            case "<", "<=", ">=", ">" -> 5;
            case "==", "!=" -> 6;
            case "&&" -> 7;
            case "||" -> 8;
            case "=", "*=", "+=", "-=", "/=", "%=" -> 9;
            default -> 0;
        };
    }
    /**
     * Вычисление выражения
     */
    Token calculate(Token tok, Token ... args){
        switch (tok.name) {
            case "/":
                return new Token(String.valueOf(parseDouble(get_value(args[1]).toFLOAT().name) / parseDouble(get_value(args[0]).toFLOAT().name)));
            case "-":
                return new Token(String.valueOf(parseDouble(get_value(args[1]).toFLOAT().name) - parseDouble(get_value(args[0]).toFLOAT().name)));
            case "+":
                return new Token(String.valueOf(parseDouble(get_value(args[1]).toFLOAT().name) + parseDouble(get_value(args[0]).toFLOAT().name)));
            case "*":
                return new Token(String.valueOf(parseDouble(get_value(args[1]).toFLOAT().name) * parseDouble(get_value(args[0]).toFLOAT().name)));
            case "**":
                return new Token(String.valueOf(Math.pow(parseDouble(get_value(args[1]).toFLOAT().name), parseDouble(get_value(args[0]).toFLOAT().name))));
            case "%":
                return new Token(String.valueOf(parseInt(get_value(args[1]).toINT().name) % parseInt(get_value(args[0]).toINT().name)));
            case "==":
                return new Token(String.valueOf(get_value(args[1]).equals(get_value(args[0]))));
            case "!=":
                return new Token(String.valueOf(get_value(args[1]).equals(get_value(args[0]))).equals("true")?"false":"true");
            case ">=":
                return new Token(String.valueOf(get_value(args[1]).compare(get_value(args[0]))>0 || get_value(args[1]).equals(get_value(args[0]))));
            case "<=":
                return new Token(String.valueOf(get_value(args[1]).compare(get_value(args[0]))<0 || get_value(args[1]).equals(get_value(args[0]))));
            case ">":
                return new Token(String.valueOf(get_value(args[1]).compare(get_value(args[0]))>0));
            case "<":
                return new Token(String.valueOf(get_value(args[1]).compare(get_value(args[0]))<0));
            case "!":
                return new Token(get_value(args[0]).toBOOL().name.equals("true")?"false":"true");
            case "&&":
                return new Token(String.valueOf(get_value(args[1]).toBOOL().name.equals("true") && get_value(args[0]).toBOOL().name.equals("true")));
            case "||":
                return new Token(String.valueOf(get_value(args[1]).toBOOL().name.equals("true") || get_value(args[0]).toBOOL().name.equals("true")));
            case "=":
                if (args[1].type == Token.Type.NONE){
                    // Пустое имя только если это переменная из массива
                    if (args[1].name.equals("NULL")){
                        Token t = get_value(args[0]);
                        args[1].name = t.name;
                        args[1].type = t.type;
                    }
                    // Если это массив как таковой, то мы не должны получать переменную отдельно
                    else if (args[0].getClass() == Array.class){
                        variables.put(args[1].name, args[0]);
                    }
                    else{
                        variables.put(args[1].name, get_value(args[0]));
                    }
                }
                return get_value(args[0]);
            case "n":
                return ((Array)args[0]).pop();
            case "~A~":
                if (args[1].getClass() == Array.class){
                    ((Array)args[1]).set_size(args[0]);
                    return args[1];
                }
                else{
                    return ((Array)get_value(args[1])).get_value(args[0]);
                }
        }
        return null;
    }
    /**
     * Если это переменная - её значение
     * Если нет - просто значение
     */
    Token get_value(Token tok){
        return variables.getOrDefault(tok.name, tok);
    }
    void STDprint(Stack<Token> values, int cnt){
        Token[] arr = new Token[cnt];
        Token temp;
        for(;cnt>0;cnt--){
            temp = get_value(values.pop());
            if (temp.getClass() == Array.class){
                arr[cnt-1] = new Token(((Array) temp).printArr());
            }
            else{
                arr[cnt-1] = new Token(temp.terminal, temp.name);
            }
            if (arr[cnt-1].type == Token.Type.STRING){
                arr[cnt-1].name = arr[cnt-1].name.substring(1, arr[cnt-1].name.length()-1);
            }
        }
        for (Token t:arr){
            System.out.print(t.name + " ");
        }
        System.out.println();
    }

    void parseLine(LinkedList<Token> input){
        Stack<Token> values = new Stack<>();
        Stack<Array> arrays = new Stack<>();
        Token current;
        if (PolishNotation.verbose){
            System.out.println("-------------- Начинаем вычисление выражения -----------");
        }
        int i=0;
        Token temp; // как обычно на всякий случай
        int[] t;
        for (;i<input.size();i++){
            current = input.get(i);
            if (PolishNotation.verbose){
                System.out.println("current: " + current.name);
            }
            if (current.terminal){
                if (current.name.equals(";")){
                    values = new Stack<>();
                }
                else if (current.name.equals("n")){
                    values.add(calculate(current, arrays.peek(), values.peek()));
                }
                else{
                    switch(current.get_params()){
                        case 1:
                            values.add(calculate(current, values.pop(), new Token("0")));
                            break;
                        case 2:
                            values.add(calculate(current, values.pop(), values.pop()));
                            break;
                        default:
                            if (PolishNotation.verbose){
                                System.out.println("А с этим я пока работать не умею: '" + current + "'");
                            }
                    }
                }
                if (current.name.equals("~A~")){
                    arrays.pop();
                }
            }
            else{
                // Если требуется переместиться куда-то
                if(current.name.equals("goto")){
                    if (values.pop().equals(new Token("false"))){
                        i = Math.min(current.get_params()-1, input.size()-1);
                    }
                }
                // Введём "стандартную" функцию print
                else if (current.name.equals("print")){
                    System.out.print("<<< ");
                    STDprint(values, current.get_params());
                }
                // А в любом другом случае нужно просто добавить переменную в стек
                else{
                    values.add(current);
                    if (get_value(current).getClass() == Array.class){
                        arrays.add((Array)get_value(current));
                    }
                }
            }
            if (PolishNotation.verbose){
                System.out.println("Стек переменных: " + values);
                System.out.println(variables);
            }
        }
        if (PolishNotation.verbose) {
            System.out.println(values);
        }
    }
    /*

PROGRAM ::= LOOP PROGRAM | APPROPRIATIONS PROGRAM | IF PROGRAM | eps
APPROPRIATIONS ::= APPROPRIATION APPROPRIATIONS | eps
APPROPRIATION ::= id = EXPRESSION; | id += EXPRESSION; | id -= EXPRESSION; | id /= EXPRESSION; | id *= EXPRESSION; | id = int ARR; | id = float ARR; | id = bool ARR; | id = string ARR;
ARR ::= [id] ARR | [id]
EXPRESSION ::= LOGIC | ARITHMETIC | VAR
ARITHMETIC ::= ARITHMETIC+ARITHMETIC | ARITHMETIC-VAR | ARITHMETIC-(ARITHMETIC) | ARITHMETIC*ARITHMETIC | ARITHMETIC/ARITHMETIC | VAR | (EXPRESSION)
LOGIC ::= EXPRESSION == EXPRESSION | EXPRESSION != EXPRESSION | EXPRESSION > EXPRESSION | EXPRESSION >= EXPRESSION | EXPRESSION < EXPRESSION | EXPRESSION <= EXPRESSION
LOOP ::= WHILE | DOWHILE | FOR
BODY ::= APPROPRIATION | LOOP | IF | {PROGRAM}
WHILE ::= while (LOGIC) BODY
DOWHILE ::= DO BODY while (LOGIC)
FOR ::= for (APPROPRIATION ; LOGIC ; APPROPRIATION) BODY
VAR ::= id | +id|-id| id ARR
IF ::= if (LOGIC) BODY ELSE
ELSE ::= eps | else BODY

Test:
id=id; if (id==id) id=id; else {id=id; id+=id*id/(id+id);}

print(a, b);

if (1+2>2){if (2==2) print("a+1==b");print("a<b");} else print("a<b-2");
over

if (2<3){
if (3>4) print("2<3 && 3>4");
else print("2<3 && 3<=4");
} else print("2>=3");
over

a=1;
b=2;
while(a<=b){
print("a is equal ", a);
a+=1;
}
over

num=16;
tmp=1;
while(tmp<num)
    tmp*=2;
if (tmp==num)
    print("Число является степенью двойки");
else
    print("Число не является степенью двойки");

a=1;
do while(a!=3) a+=1; while(a<2)
over

a=1;
do a+=1; while(a<3)
over

a=3;
do {
    a-=1;
    print(a);
} while (a>0 && a<3)
over

for (i=0;i<10;i+=1)
    print(i);
over

a = int[10][10][10];
a[1][1][1] = 11101;
print("Берём в позиции (1, 1, 1): '", a[1][1][1], "'");
print("И пытаемся взять в позиции, где пусто: '", a[1][2][3], "'");
over

for (i=0;i<3;i+=1)
for(j=0;j<3;j+=1)
print(i, j, i*j);
over

a = float[2][5];
a[0][0] = 1;
a[1][0] = 3;
for (i=1;i<5;i+=1){
	a[0][i] = a[1][i-1] - a[0][i-1];
	a[1][i] = a[0][i-1] - a[1][i-1];
	print(a[0][i], a[1][i]);
}
print(a);
over
    */

    /**
     * Преобразование выражения в обратную польскую запись
     */
    Stack<Token> EquationToPolish(LinkedList<Token> input){
        Stack<Token> output = new Stack<>(); // массив выхода
        Stack<Token> operations = new Stack<>(); // массив текущих операций
        Token current; // текущий рассматриваемый символ строки
        Token temp; // И промежуточный, иногда используемся
        Stack<Token> Movements = new Stack<>();
        final HashSet<String> arrays = new HashSet<>();
        arrays.add("int"); arrays.add("string");
        arrays.add("float"); arrays.add("bool");
        label:
        while(!input.isEmpty()){
            current = input.pop();
            if (PolishNotation.verbose){
                System.out.println("Текущий элемент: " + current);
            }
            // Если это или имя переменной, или число - заносим в output
            if (!current.terminal){
                if (functions.contains(current.name)){
                    operations.add(current);
                }
                else if (!input.isEmpty() && input.peek().name.equals("[")){
                    operations.add(new Array(current));
                    if (arrays.contains(current.name)){
                        output.add(operations.peek());
                    }
                    else{
                        output.add(current);
                    }
                    // Указатель на имя массива кидаем и туда и туда
                }
                else{
                    output.add(current);
                }
            }
            // В случае с if нужно запомнить "перемещатель"
            else if (current.name.equals("if")){
                operations.add(new Token("if"));
                Movements.add(new Token("goto"));
            }
            // Если else, то нужно добавить безусловный переход
            else if (current.name.equals("else")){
                operations.add(new Token("else"));
                output.add(new Token("false"));
                Movements.add(new Token("goto"));
                output.add(Movements.peek());
            }
            else if (current.name.equals("do")){
                operations.add(new Token("do"));
                Movements.add(new Token("goto"));
                Movements.peek().set_params(output.size());
            }
            else if (current.name.equals("while")){
                // Если на вершине стека лежит do
                if (!operations.isEmpty() && operations.peek().name.equals("do") && operations.peek().get_params()!=0){
                    operations.add(new Token("while"));
                }
                else {
                    operations.add(new Token("while"));
                    // Сначала добавляем goto, чтобы зациклится
                    Movements.add(new Token("goto"));
                    Movements.peek().set_params(output.size());
                    // а затем тот, что будет вести из цикла
                    Movements.add(new Token("goto"));
                }
            }
            else if (current.name.equals("for")){
                operations.add(new Token(false, "for", 1));
                // цикл for непросто разобрать, поэтому эта число параметров считает где что сделать
                // Если она равна 1-до первого ';', 2 - до второго ';', 3 - до конца, 4 - сам блок
                // Съедаем следующую скобку
                if (input.isEmpty() || !input.pop().name.equals("(")){
                    System.out.println("Забыли скобку");
                    break;
                }
            }
            // Если это скобочка '(', просто кидаем в стек
            else if (current.name.equals("(") || current.name.equals("{") || current.name.equals("[")){
                operations.add(current);
            }
            // Если это разделитель аргументов функции, то последовательно переносим аргументы в output
            else if (current.name.equals(",")){
                while(!operations.isEmpty() && !operations.peek().name.equals("(")){
                    output.add(operations.pop());
                }
                if (!operations.isEmpty()){
                    temp = operations.pop();
                    if (!operations.isEmpty() && !operations.peek().terminal){
                        operations.peek().set_params(operations.peek().get_params()+1);
                    }
                    operations.add(temp);
                }
            }
            // Если это скобка ')' или ']', то переносим в output и удаляем открывающуюся скобку из стека
            else if (current.name.equals(")") || current.name.equals("]")){
                while(!operations.isEmpty() && !operations.peek().name.equals("(") &&
                        !operations.peek().name.equals("{") && !operations.peek().name.equals("for") &&
                        !operations.peek().name.equals("[")){
                    temp = operations.pop();
                    if (temp.name.equals("+=") || temp.name.equals("-=") || temp.name.equals("/=") || temp.name.equals("*=") || temp.name.equals("%=")){
                        temp = new Token(true, temp.name.substring(0, 1));
                        temp.set_params(2);
                        output.add(temp);
                        temp = new Token(true, "=");
                        temp.set_params(2);
                    }
                    output.add(temp);
                }
                if (!operations.isEmpty()){
                    if (operations.peek().name.equals("(") || operations.peek().name.equals("["))
                        operations.pop();
                    if (!Movements.isEmpty()){
                        if (operations.peek().name.equals("if") || operations.peek().name.equals("else")){
                            output.add(Movements.peek());
                        }
                        else if (operations.peek().name.equals("while")){
                            operations.pop();
                            if (!operations.isEmpty() && operations.peek().name.equals("do") && operations.peek().get_params()!=0){
                                operations.pop();
                                output.add(new Token(true, "!", 1));
                                output.add(Movements.pop());
                            }
                            else{
                                operations.add(new Token("while"));
                                output.add(Movements.peek());
                            }
                        }
                        else if (operations.peek().name.equals("for") && operations.peek().get_params() == 3){
                            // Уходит первый
                            output.add(new Token("false"));
                            output.add(Movements.pop());
                            // Созраняем третий
                            temp = Movements.pop();
                            // Уходит второй
                            Movements.pop().set_params(output.size());
                            // Возвращаем третий
                            Movements.add(temp);
                            operations.peek().set_params(4);
                        }
                    }
                    // И если нашли нетерминал на вершине стека - переносим его в output
                    if (!operations.isEmpty() && functions.contains(operations.peek().name)){
                        operations.peek().set_params(operations.peek().get_params()+1);
                        output.add(operations.pop());
                    }
                    // Если это массив
                    else if (!operations.isEmpty() && current.name.equals("]")){
                        // Если это ещё не конец перечисления параметров
                        if (!input.isEmpty() && input.peek().name.equals("[")){
                            // Если мы уже проинициализировали его когда-то, то мы не должны его создавать
                            if (!arrays.contains(operations.peek().name)){
                                if (operations.peek().get_params()!=0){
                                    output.add(new Token(true, "n", 1));
                                }
                            }
                            else {
                                ((Array)operations.peek()).add_val(output.peek());
                            }
                            operations.peek().set_params(operations.peek().get_params()+1);
                        }
                        else{
                            // Если это конец и массив уже проинициализирован
                            if (!arrays.contains(operations.peek().name)){
                                if (operations.peek().get_params()!=0) {
                                    output.add(new Token(true, "n", 1));
                                }
                                operations.peek().set_params(operations.peek().get_params()+1);
                                for (int i=0;i<operations.peek().get_params()-1;i++){
                                    output.add(new Token(true, "*", 2));
                                    output.add(new Token(true, "+", 2));
                                }
                            }
                            else{
                                ((Array)operations.peek()).add_val(output.peek());
                                operations.peek().set_params(operations.peek().get_params()+1);
                                for (int i=0;i<operations.peek().get_params()-1;i++){
                                    output.add(new Token(true, "*", 2));
                                }
                            }
                            output.add(new Token(true, "~A~", 2));
                            operations.pop();
                        }
                    }
                }
            }
            else if (current.name.equals("}")){
                while(!operations.isEmpty() && !operations.peek().name.equals("(") && !operations.peek().name.equals("{")){
                    output.add(operations.pop());
                }
                if (!operations.isEmpty()) {
                    operations.pop();
                    // Если это было завершение цикла или условия, то теперь мы знаем длину для goto
                    if (!operations.isEmpty() && !Movements.isEmpty()){
                        // Если это условие, то нам просто нужно дать goto переход в текущую точку
                        switch (operations.peek().name) {
                            case "if":
                            case "else":
                                operations.pop();
                                Movements.pop().set_params(output.size() + (!input.isEmpty() && input.getFirst().name.equals("else") ? 2 : 0));
                                break;
                            // Если же это цикл, то всё чуть сложнее, ибо 1 переход назад, а другой ведёт вперёд
                            case "while":
                                operations.pop();
                                Movements.pop().set_params(output.size() + 2);
                                output.add(new Token("false"));
                                output.add(Movements.pop());
                                break;
                            case "do":
                                operations.peek().set_params(1);
                                break;
                            case "for":
                                if (operations.peek().get_params() == 1) {
                                    // Я пронумеровал goto, чтобы не запутаться в них
                                    // Тот, что уйдёт первым
                                    Movements.add(new Token("goto"));
                                    Movements.peek().set_params(output.size());
                                    operations.peek().set_params(2);
                                    break label;
                                } else if (operations.peek().get_params() == 2) {
                                    output.pop(); // там лежит ненужная точка с запятой

                                    temp = Movements.pop();
                                    // Тот, что уйдёт четвёртым
                                    Movements.add(new Token("goto"));
                                    output.add(Movements.peek());
                                    // Тот, что уйдёт вторым
                                    Movements.add(new Token("goto"));
                                    output.add(new Token("false"));
                                    output.add(Movements.peek());
                                    // Тот, что уйдёт третьим
                                    Movements.add(new Token("goto"));
                                    Movements.peek().set_params(output.size());
                                    Movements.add(temp);
                                    operations.peek().set_params(3);
                                    break label;
                                } else if (operations.peek().get_params() == 4) {
                                    operations.peek().set_params(0);
                                    operations.pop();
                                    // Уходит третий
                                    output.add(new Token("false"));
                                    output.add(Movements.pop());
                                    // Уходит последний
                                    Movements.pop().set_params(output.size());
                                }
                                break;
                        }
                    }
                }
            }
            // После ; и нужно очистить стек
            else if (current.name.equals(";")){
                // Если в стеке ещё остались операторы
                while(!operations.isEmpty() && !operations.peek().name.equals("if") && !operations.peek().name.equals("else") &&
                        !operations.peek().name.equals("while") && !operations.peek().name.equals("do") && !operations.peek().name.equals("for")){
                    if (operations.peek().name.equals("{") && !input.isEmpty()) {
                        break;
                    }
                    if ((operations.peek().name.equals("(") || operations.peek().name.equals("{")) && input.isEmpty()){
                        System.out.println("Незакрытая скобка!");
                        break;
                    }
                    else{
                        // Перекладываем операторы в output
                        temp = operations.pop();
                        if (temp.name.equals("+=") || temp.name.equals("-=") || temp.name.equals("/=") || temp.name.equals("*=") || temp.name.equals("%=")){
                            temp = new Token(true, temp.name.substring(0, 1));
                            temp.set_params(2);
                            output.add(temp);
                            temp = new Token(true, "=");
                            temp.set_params(2);
                        }
                        output.add(temp);
                    }
                }
                output.add(current);
                // Нужно убрать goto из стека где-то сейчас
                label1:
                while (!operations.isEmpty() && (operations.peek().name.equals("if") || operations.peek().name.equals("else") ||
                        operations.peek().name.equals("while") || operations.peek().name.equals("do") || operations.peek().name.equals("for"))){
                    // Если это условие, то нам просто нужно дать goto переход в текущую точку
                    switch (operations.peek().name) {
                        case "if":
                        case "else":
                            operations.pop();
                            Movements.pop().set_params(output.size() + (!input.isEmpty() && input.getFirst().name.equals("else") ? 2 : 0));
                            break;
                        // Если же это цикл, то всё чуть сложнее, ибо 1 переход назад, а другой ведёт вперёд
                        case "while":
                            operations.pop();
                            Movements.pop().set_params(output.size() + 2);
                            output.add(new Token("false"));
                            output.add(Movements.pop());
                            break;
                        case "do":
                            operations.peek().set_params(1);
                            break label1;
                        case "for":
                            if (operations.peek().get_params() == 1) {
                                // Я пронумеровал goto, чтобы не запутаться в них
                                // Тот, что уйдёт первым
                                Movements.add(new Token("goto"));
                                Movements.peek().set_params(output.size());
                                operations.peek().set_params(2);
                                break label1;
                            } else if (operations.peek().get_params() == 2) {
                                output.pop(); // там лежит ненужная точка с запятой

                                temp = Movements.pop();
                                // Тот, что уйдёт четвёртым
                                Movements.add(new Token("goto"));
                                output.add(Movements.peek());
                                // Тот, что уйдёт вторым
                                Movements.add(new Token("goto"));
                                output.add(new Token("false"));
                                output.add(Movements.peek());
                                // Тот, что уйдёт третьим
                                Movements.add(new Token("goto"));
                                Movements.peek().set_params(output.size());
                                Movements.add(temp);
                                operations.peek().set_params(3);
                                break label1;
                            } else if (operations.peek().get_params() == 4) {
                                operations.peek().set_params(0);
                                operations.pop();
                                // Уходит третий
                                output.add(new Token("false"));
                                output.add(Movements.pop());
                                // Уходит последний
                                Movements.pop().set_params(output.size());
                            }
                            break;
                    }
                }
            }
            // Если это оператор (а в моём решении все операторы - терминалы)
            else {
                if (current.name.equals("+=") || current.name.equals("-=") || current.name.equals("/=") || current.name.equals("*=") || current.name.equals("%=")){
                    output.add(output.peek()); // дублируем переменную
                }
                // Пока присутствует на вершине стека токен оператор op2,
                // чей приоритет выше или равен приоритету op1,
                // и при равенстве приоритетов op1 является левоассоциативным
                while(!operations.isEmpty() && operations.peek().terminal && !operations.peek().name.equals("(") && !operations.peek().name.equals("{") &&
                        !operations.peek().name.equals("[") &&
                        (priority(operations.peek().name) <= priority(current.name) ||
                                (priority(operations.peek().name) == priority(current.name) && current.leftAssoc))){
                    // Перекладываем операторы в output
                    temp = operations.pop();
                    System.out.println(temp);
                    if (temp.name.equals("+=") || temp.name.equals("-=") || temp.name.equals("/=") || temp.name.equals("*=") || temp.name.equals("%=")){
                        temp = new Token(true, temp.name.substring(0, 1));
                        temp.set_params(2);
                        output.add(temp);
                        temp = new Token(true, "=");
                        temp.set_params(2);
                    }
                    output.add(temp);
                }
                operations.add(current);
            }
            if (PolishNotation.verbose){
                System.out.println("Операции: " + operations);
                System.out.println("Выход: " + output);
                System.out.println("GotoList: " + Movements + "\n");
            }
        }
        // Возвращааем полученный стек выхода
        return output;
    }
}
