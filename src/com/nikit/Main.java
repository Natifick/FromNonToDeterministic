package com.nikit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import static java.lang.String.valueOf;


class Transition{
    String label = "";
    Node from;
    LinkedList<Node>to;
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

class MyList<T> extends LinkedList<T>{
    public boolean containsOne(LinkedList<T> col) {
        for (T t : col) {
            if (super.contains(t)) {
                return true;
            }
        }
        return false;
    }
}

class FiniteAutomation{
    LinkedList<Transition> trans;
    LinkedList<Node> nodes;

    public FiniteAutomation(LinkedList<Node> n, LinkedList<Transition> tr){
        trans = new LinkedList<>();
        nodes = n;

        // Объединяем, если какие-то 2 связи имеют 1 метку и исходят из одной ноды
        boolean fl = false;
        for (Transition transition : tr) {
            fl = false;
            for (Transition tran : trans) {
                if (tran.from.equals(transition.from) && tran.label.equals(transition.label)) {
                    fl = true;
                    tran.add(transition.to);
                }
            }
            if (!fl) {
                trans.add(transition);
            }
        }
    }

    public Node get(Node n, String label){
        for (Transition tr: trans){
            if (tr.from == n && tr.label.equals(label)){
                return tr.to.get(0);
            }
        }
        return null;
    }

    public void Minimize(){
        Minimize(nodes.size());
    }

    /** Минимизация детерминированного автомата */
    public void Minimize(int s){

        // Чтобы можно было любые метки использовать
        LinkedList<String> labels = new LinkedList<>();
        for (Transition t: trans){
            if (!labels.contains(t.label)){
                labels.add(t.label);
            }
        }
        for (Node n: nodes){ // Обнуляем индексы, в них будем записывать всю информацию о сокращениях
            n.idx = new MyList<>();
            for (int i=0;i<labels.size();i++){
                n.idx.add(0);
            }
        }


        for (Transition t: trans){
            t.from.idx.set(labels.indexOf(t.label), t.to.get(0).fin?1:0);
        }

        for (int i=0;i<nodes.size();i++){
            for (int j=i+1;j<nodes.size();j++){
                // Если ноды соеражт одинаковые переходы
                if (nodes.get(i).idx.equals(nodes.get(j).idx) && nodes.get(i).fin == nodes.get(j).fin){
                    for (int t=0;t<trans.size();t++){
                        // То все переходы из этой ноды мы просто удаляем
                        if (trans.get(t).from == nodes.get(j)){
                            trans.remove(t);
                            t--;
                            continue;
                        }
                        // А все переходы в эту ноду заменяем переходами в подобную
                        if (trans.get(t).to.get(0) == nodes.get(j)){
                            trans.get(t).to.remove(nodes.get(j));
                            trans.get(t).to.add(nodes.get(i));
                            t--;
                        }
                    }
                    nodes.remove(j);
                    j--;
                }
            }
        }
        System.out.println("Ноды и переходы в процессе минимизации");
        System.out.println(nodes);
        System.out.println(trans);
        if (nodes.size()<s){
            Minimize(nodes.size());
        }
        else{
            System.out.println("Конец минимизации");
        }
    }


    MyList<Integer> get_idx(Node node, LinkedList<Node> t){
        t.add(node);
        HashSet<Integer> temp = new HashSet<>();
        for (int j=0;j<trans.size();j++){
            if (trans.get(j).to.contains(node)){
                if (!trans.get(j).label.equals("")){
                    temp.add(j+1);
                }
                else if (!t.contains(trans.get(j).from)){
                    if (trans.get(j).from.idx.isEmpty()){
                        temp.addAll(get_idx(trans.get(j).from, (LinkedList<Node>)t.clone()));
                    }
                    else{
                        temp.addAll(trans.get(j).from.idx);
                    }
                }
            }
        }
        node.idx.addAll(temp);
        return node.idx;
    }

    /** Построение детерминированного автомата из недетерминированного */
    void ToDetermined(){
        // переиспользуем переменную, на самом деле она будет нужна позднее
        LinkedList<Node> newNodes;
        // проходимся по всем нодам и приписываем к ним индексы (в последний столбец в тетради)
        for (Node value : nodes) {
            if (value.idx.isEmpty()) {
                newNodes = new LinkedList<>();
                newNodes.add(value);
                value.idx = get_idx(value, newNodes);
            }
        }

        LinkedList<Transition> newTrans = new LinkedList<>();
        newNodes = new LinkedList<>();// Обнуляем переменную

        nodes.get(0).idx.addFirst(0);
        System.out.println("Переходы после индексации (нулевой добавляется фиктивно):");
        System.out.println(trans);
        System.out.println("Ноды после индексации:");
        System.out.println(nodes);

        Node tmp;
        boolean fl;
        for (int i: nodes.get(0).idx){
            // Первые несколько строк новой таблицы - индексы первой строки первой таблицы
            newNodes.add((trans.get(i).from).clone()); // При клонировании она полностью очищается
            newNodes.getLast().idx.add(i); // шифры пока кидаем в idx
            for (int j=0;j<trans.size();j++){
                // Определили, что текущий номер из индекса ноды есть в индексе какой-то ещё ноды
                if (trans.get(j).from.idx.contains(i) && !trans.get(j).label.equals("")){
                    fl = false;
                    for (int k=0;k<newTrans.size();k++){
                        if (newTrans.get(k).from.idx.equals(newNodes.getLast().idx) && newTrans.get(k).label.equals(trans.get(j).label)){
                            tmp = new Node("");
                            tmp.idx.add(j+1);
                            newTrans.get(k).to.add(tmp);
                            fl = true;
                            break;
                        }
                    }
                    if (!fl){
                        tmp = new Node("");
                        tmp.idx.add(j+1);
                        newTrans.add(new Transition(trans.get(j).label, newNodes.getLast().copy(), tmp));
                    }
                }
            }
        }

        System.out.println("Переходы после создания второй таблицы:");
        System.out.println(newTrans);
        System.out.println("Ноды после создания второй таблицы:");
        System.out.println(newNodes);

        // Переменная для подсчёта количества уже пройденных ячеек
        int s = newNodes.size();
        MyList<Integer> temp;
        while (true){

            // Проверяем, все ли состояния, в которые можно перейти у нас уже заведены
            for (Transition tran : newTrans) {
                // Для этого во всех переходах собираем "имя" состояния, состоящее из индексов
                temp = new MyList<>();
                for (int k = 0; k < tran.to.size(); k++) {
                    temp.addAll(tran.to.get(k).idx);
                }
                // И проверяем, есть ли уже метка с таким именем?
                fl = false;
                for (Node newNode : newNodes) {
                    // если текущая метка и правда носит такое имя (а оно, опять-таки, лежит в индексе)
                    if (newNode.idx.equals(temp)) {
                        fl = true;
                        break;
                    }
                }
                // Если на предыдущем шаге мы не нашли состояние с нужным именем
                if (!fl) {
                    tmp = new Node("");
                    tmp.idx = temp;
                    newNodes.add(tmp);
                }
            }

            // Если оказалось, что на этом шаге новых нод не добавилось
            if (s>=newNodes.size()){
                break;
            }

            // По всем непройденным нодам ищем, куда можем из них перейти
            for (s=s;s<newNodes.size();s++){
                for (int j=0;j<trans.size();j++){
                    // Определили, что текущий номер из индекса ноды есть в индексе какой-то ещё ноды
                    if (trans.get(j).from.idx.containsOne(newNodes.get(s).idx) && !trans.get(j).label.equals("")){
                        fl = false;
                        for (Transition tr : newTrans) {
                            if (tr.from.idx.equals(newNodes.get(s).idx) && tr.label.equals(trans.get(j).label)) {
                                tmp = new Node("");
                                tmp.idx.add(j + 1);
                                tr.to.add(tmp);
                                fl = true;
                                break;
                            }
                        }
                        // Если мы не нашли уже существующего пути с такой меткой и таким выходом
                        if (!fl){
                            tmp = new Node("");
                            tmp.idx.add(j+1);
                            System.out.println("Текущая нода: " + newNodes.get(s).copy() + " " + tmp);
                            newTrans.add(new Transition(trans.get(j).label, newNodes.get(s).copy(), tmp));
                        }
                    }
                }
            }
        }

        for (int i=0;i<newNodes.size();i++){
            newNodes.get(i).name = valueOf(i); // Присваиваем им равное индексу
            for (Node node : nodes) { // Финальная ли эта нода?
                // Нода финальна, если её индекс содержит индекс из финальной ноды
                if (node.fin && newNodes.get(i).idx.containsOne(node.idx)) {
                    newNodes.get(i).fin = true;
                }
            }
        }

        System.out.println("Переходы без вычеркнутых лишних строк:");
        System.out.println(newTrans);
        System.out.println("Ноды без вычеркнутых лишних:");
        System.out.println(newNodes);

        for (Node newNode : newNodes) {
            // Пытаемся также заменить такую же ноду в переходах на эту ноду (создаём ссылки)
            for (Transition t : newTrans) {
                // переход из этой ноды
                if (t.from.idx.containsAll(newNode.idx) && newNode.idx.containsAll(t.from.idx)) {
                    t.from = newNode;
                }
                temp = new MyList<>();
                for (Node n : t.to) {
                    temp.addAll(n.idx);
                }
                if (temp.containsAll(newNode.idx) && newNode.idx.containsAll(temp)) { // Переход в эту ноду
                    t.to = new LinkedList<>();
                    t.to.add(newNode);
                }
            }
            newNode.idx = new MyList<>(); // Сразу обнуляем индекс
        }

        // Убираем одинаковые строки
        for (int t1=0;t1<newTrans.size();t1++){
            for (int t2=0;t2<newTrans.size();t2++){
                // Если переходы совпадают по всем параметрам, то записываем это в ихиндексы
                if (newTrans.get(t1).from.fin == newTrans.get(t2).from.fin &&
                        newTrans.get(t1).to.equals(newTrans.get(t2).to) &&
                        t1!=t2){
                    // Для первой ноды
                    if (!newTrans.get(t1).from.idx.contains(t2)){
                        newTrans.get(t1).from.idx.add(t2);
                    }
                    if (!newTrans.get(t1).from.idx.contains(t1)){
                        newTrans.get(t1).from.idx.add(t1);
                    }
                    // Для второй ноды
                    if (!newTrans.get(t1).from.idx.contains(t2)){
                        newTrans.get(t1).from.idx.add(t2);
                    }
                    if (!newTrans.get(t1).from.idx.contains(t1)){
                        newTrans.get(t1).from.idx.add(t1);
                    }
                }
            }
        }

        System.out.println("Переходы прямо перед вычёркиванием:");
        System.out.println(newTrans);
        System.out.println("Ноды прямо перед вычёркиванием:");
        System.out.println(newNodes);

        for (int i=0;i<newNodes.size();i++){
            for (int j=i+1;j<newNodes.size();j++){
                // Если ноды содержат одинаковые переходы
                if (newNodes.get(i).idx.containsAll(newNodes.get(j).idx) &&
                        newNodes.get(j).idx.containsAll(newNodes.get(i).idx) && newNodes.get(j).fin == newNodes.get(i).fin){
                    for (int t=0;t<newTrans.size();t++){
                        // То все переходы из этой ноды мы просто удаляем
                        if (newTrans.get(t).from == newNodes.get(j)){
                            newTrans.remove(t);
                            t--;
                            continue;
                        }
                        // А все переходы в эту ноду заменяем переходами в подобную
                        if (newTrans.get(t).to.get(0) == newNodes.get(j)){
                            newTrans.get(t).to.remove(newNodes.get(j));
                            newTrans.get(t).to.add(newNodes.get(i));
                            t--;
                        }
                    }
                    newNodes.remove(j);
                    j--;
                }
            }
        }
        nodes = newNodes;
        trans = newTrans;

        System.out.println("Переходы в конце:");
        System.out.println(trans);
        System.out.println("Ноды в конце:");
        System.out.println(nodes);
        //Minimize();
    }
}

class Token{
    boolean terminal = false;
    String name;
    public Token(boolean terminal, String name) {
        this.terminal = terminal;
        this.name = name;
    }
    public Token(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "Token{" +
                "terminal=" + terminal +
                ", name='" + name + '\'' +
                '}';
    }
}

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
        }
        if (t.length() != 0){
            output.add(new Token(terminal, t.toString()));
        }
        System.out.println("Токены для строки:\t" + output);
        return output;
    }
}
/*
To test it:
S ::= A;
A ::= E
A ::= +E
A ::= A+E
A ::= A*E
E ::= id
E ::= (A)
exit
*/
class Main{
    static LinkedList<Node> nodes;
    static LinkedList<Transition> trans;
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        LinkedList<String> names = new LinkedList<>();
        LinkedList<LinkedList<Token>> temporal = new LinkedList<>(); // Храним для последующего развёртывания
        while (sc.hasNextLine()){
            String[] temp = sc.nextLine().split(" ::= ");
            if (temp[0].equals("exit")){
                break;
            }
            if (!names.contains(temp[0])){
                names.add(temp[0]);
            }
            temporal.add(Tokenizer.tokenize(temp[1]));
            for (Token i: temporal.getLast()){ // Исследуем правую часть
                if (!i.terminal && !names.contains(i.name)){ // Если ещё не встречали символ справа
                    names.add(i.name);
                }
            }
        }
        nodes = new LinkedList<>();
        trans = new LinkedList<>();
        String label = "";
        for (int i=0;i<temporal.size();i++){
            label = "";
            int cnt = 0;
            for (int j=0;j<temporal.get(i).size();j++){
                if (temporal.get(i).get(j).terminal){
                    label = temporal.get(i).get(j).name;
                }
                else{
                    trans.add(new Transition(label, new Node(valueOf(cnt) + "," + valueOf(i)), new Node(valueOf(++cnt) + "," + valueOf(i))));
                }
            }
        }
        for (Transition t: trans){
            nodes.add(t.from);
            nodes.addAll(t.to);
        }
        System.out.println(trans);
        System.out.println(nodes);

        newFrame f = new newFrame();
        f.print_automaton(new FiniteAutomation(nodes, trans));
    }
}


/*
class Main extends JFrame {
    JPanel top, middle1, middle2, bottom;
    JButton confirm1, determ, minim;
    JFormattedTextField rows, cols;
    int r, c;
    JPanel[] pans;

    JLabel info;
    JFormattedTextField[] flds;

    public static void main(String[] args){
        new Main();
        System.out.println((char)('A'+2));
    }

    void FillTheTable(int rows, int cols){
        setSize(600, 700);
        info.setVisible(true);
        flds = new JFormattedTextField[(rows+1)*(cols+2)];

        bottom.setLayout(new GridLayout(rows+1, cols+3));
        pans = new JPanel[(rows+1)*(cols+3)];
        for (int i=0;i<pans.length;i++){
            pans[i] = new JPanel();
        }
        pans[0].add(new JLabel("Q"));
        pans[1].add(new JLabel("F"));
        pans[cols+2].add(new Label("eps"));
        for (int i=2;i<cols+2;i++){
            pans[i].add(new JLabel("" +(i-2)));
        }
        for (int i=1;i<rows+1;i++){
            pans[i*(cols+3)].add(new JLabel("" + (i-1)));
            for (int j=1;j<cols+3;j++){
                flds[(i-1)*(cols+2)+j-1] = new JFormattedTextField();
                flds[(i-1)*(cols+2)+j-1].setPreferredSize(new Dimension(50, 20));
                pans[i*(cols+3)+j].add(flds[(i-1)*(cols+2)+j-1]);
            }
        }
        for (JPanel pan : pans) {
            bottom.add(pan);
        }
        add(bottom);
    }

    public Main(){
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        setSize(500, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        top = new JPanel();
        top.setLayout(new GridLayout(2, 2));
        rows = new JFormattedTextField();
        cols = new JFormattedTextField();
        JLabel tmp = new JLabel("Число состояний:");
        tmp.setHorizontalAlignment(JLabel.CENTER);
        top.add(tmp);
        tmp = new JLabel("Число меток переходов:");
        tmp.setHorizontalAlignment(JLabel.CENTER);
        top.add(tmp);
        top.add(new JLabel());
        top.add(rows);
        top.add(cols);
        top.setPreferredSize(new Dimension(500, 200));
        add(top);

        middle1 = new JPanel();
        confirm1 = new JButton();
        confirm1.setText("Подтвердить");
        confirm1.setHorizontalAlignment(JButton.CENTER);
        confirm1.setVerticalAlignment(JButton.CENTER);
        confirm1.addActionListener(e -> {
            if (!rows.getText().equals("") && !cols.getText().equals("")){
                r = Integer.parseInt(rows.getText());
                c = Integer.parseInt(cols.getText());
                middle1.setVisible(false);
                middle1.setEnabled(false);
                minim.setVisible(true);
                minim.setEnabled(true);
                determ.setVisible(true);
                determ.setEnabled(true);
                FillTheTable(r, c);
                remove(top);
                System.out.println("Well done");
            }
        });
        middle1.add(confirm1);
        middle1.setPreferredSize(new Dimension(500, 100));
        add(middle1);

        middle2 = new JPanel();
        info = new JLabel("Если несколько переходов с одной меткой, вводите их через пробел");
        info.setVisible(false);
        middle2.add(info);
        determ = new JButton();
        determ.setText("Детерминировать");
        determ.setHorizontalAlignment(JButton.CENTER);
        determ.setVerticalAlignment(JButton.CENTER);
        determ.setVisible(false);
        determ.setEnabled(false);
        determ.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinkedList<Node> n = new LinkedList<>();
                Node temp;
                for (int i=0;i<r;i++){
                    temp = new Node(valueOf(i));
                    temp.fin = flds[(c+2)*i].getText().equals("1");
                    System.out.println();
                    n.add(temp);
                }
                LinkedList<Transition> tr = new LinkedList<>();
                String t;
                for (int i=0;i<r;i++) {
                    for (int j = 1; j < c+2; j++) {
                        t = flds[i * (c + 2) + j].getText();
                        if (!t.equals("")){
                            for (String s : t.split(" ")) {
                                if (j!=c+1){
                                    tr.add(new Transition(valueOf((char)('A'+j-1)), n.get(i), n.get(Integer.parseInt(s))));
                                }
                                else{
                                    tr.add(new Transition("", n.get(i), n.get(Integer.parseInt(s))));
                                }
                            }
                        }
                    }
                }

                FiniteAutomation FA = new FiniteAutomation(n, tr);
                FA.ToDetermined();
                newFrame fr = new newFrame();
                fr.print_automaton(FA);
            }
        });
        middle2.add(determ);

        minim = new JButton();
        minim.setText("Минимизировать");
        minim.setHorizontalAlignment(JButton.CENTER);
        minim.setVerticalAlignment(JButton.CENTER);
        minim.setVisible(false);
        minim.setEnabled(false);
        minim.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinkedList<Node> n = new LinkedList<>();
                Node temp;
                for (int i=0;i<r;i++){
                    temp = new Node(valueOf(i));
                    temp.fin = flds[(c+2)*i].getText().equals("1");
                    System.out.println();
                    n.add(temp);
                }
                LinkedList<Transition> tr = new LinkedList<>();
                String t;
                for (int i=0;i<r;i++) {
                    for (int j = 1; j < c+2; j++) {
                        t = flds[i * (c + 2) + j].getText();
                        if (!t.equals("")){
                            for (String s : t.split(" ")) {
                                if (j!=c+1){
                                    tr.add(new Transition(valueOf(j-1), n.get(i), n.get(Integer.parseInt(s))));
                                }
                                else{
                                    tr.add(new Transition("", n.get(i), n.get(Integer.parseInt(s))));
                                }
                            }
                        }
                    }
                }
                FiniteAutomation FA = new FiniteAutomation(n, tr);
                FA.Minimize();
                newFrame fr = new newFrame();
                fr.print_automaton(FA);
            }
        });
        middle2.add(minim);
        middle2.setPreferredSize(new Dimension(600, 100));
        add(middle2);
        bottom = new JPanel();
        bottom.setPreferredSize(new Dimension(600, 600));
        setVisible(true);
    }

}
 */

class newFrame extends JFrame{
    JLabel[] flds;
    final String REPLACE_VALUE = "";
    void print_automaton(FiniteAutomation FA){
        int rows, cols;
        setSize(600, 700);
        LinkedList<Node> nodes = FA.nodes;
        LinkedList<Transition> tr = FA.trans;
        LinkedList<String> labels = new LinkedList<>();
        for (Transition t: tr){
            if (!labels.contains(t.label)){
                labels.add(t.label);
            }
        }
        rows = nodes.size()+1;
        cols = labels.size()+2;
        System.out.println(rows + " " + cols);
        flds = new JLabel[rows*cols];

        setLayout(new GridLayout(rows, cols));
        for (int i=0;i<flds.length;i++){
            flds[i] = new JLabel();
        }
        flds[0].setText("Q");
        flds[1].setText("F");
        for (int i=2;i<cols;i++){
            flds[i].setText("" + labels.get(i-2));
        }
        Node temp;

        for (int i=1;i<rows;i++){
            flds[i*cols].setText("" + nodes.get(i-1).name);
            flds[i*cols+1].setText("" + (nodes.get(i-1).fin?1:0));
            for (int j=2;j<cols;j++){
                flds[i*cols+j] = new JLabel();
                flds[i*cols+j].setPreferredSize(new Dimension(50, 20));
                temp = FA.get(nodes.get(i-1), labels.get(j-2));
                flds[i*cols+j].setText(temp!=null?temp.name:REPLACE_VALUE);
            }
        }
        for (JLabel lb : flds) {
            add(lb);
        }
        setVisible(true);
    }
}