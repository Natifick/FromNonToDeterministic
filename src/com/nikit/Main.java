package com.nikit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

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
        return "\n\tTransition={label=" + label + ", from=" + from + ", to=" + to + '}';
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

    /** Минимизация детерминированного автомата */
    public void Minimize(int s){
        // На вход поступает только детерминированный автомат, а он полный, поэтому можно узнать число меток перходов:
        int count = (int)(trans.size()/(float)nodes.size());
        for (Node n: nodes){ // Обнуляем индексы, в них будем записывать всю информацию о сокращениях
            n.idx = new MyList<>();
            for (int i=0;i<count;i++){
                n.idx.add(0);
            }
        }

        for (Transition t: trans){
            t.from.idx.set(Integer.parseInt(t.label), t.to.get(0).fin?1:0);
        }
        //TODO  //----- Убрать все одинаковые строки -----//
        for (int i=0;i<nodes.size();i++){
            for (int j=i+1;j<nodes.size();j++){
                // Если ноды соеражт одинаковые переходы
                if (nodes.get(i).idx.equals(nodes.get(j).idx) && nodes.get(i).fin == nodes.get(j).fin){
                    for (int t=0;t<trans.size();t++){
                        // То все переходы из этой ноды мы просто удаляем
                        if (trans.get(t).from == nodes.get(j)){
                            trans.remove(t);
                            t--;
                        }
                    }
                    nodes.remove(i);
                    i--;
                }
            }
        }
        System.out.println(nodes);
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
        System.out.println("Переходы в конце:");
        System.out.println(newTrans);
        System.out.println("Ноды в конце:");
        System.out.println(newNodes);

        for (int i=0;i<newNodes.size();i++){
            newNodes.get(i).name = valueOf(i); // Присваиваем им равное индексу
            for (Node node : nodes) { // Финальная ли эта нода?
                // Нода финальна, если её индекс содержит индекс из финальной ноды
                if (node.fin && newNodes.get(i).idx.containsOne(node.idx)) {
                    newNodes.get(i).fin = true;
                }
            }
        }
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
        for (int i=0;i<newNodes.size();i++){
            for (int j=i+1;j<newNodes.size();j++){
                // Если ноды соеражт одинаковые переходы
                if (newNodes.get(i).idx.containsAll(newNodes.get(j).idx) &&
                        newNodes.get(j).idx.containsAll(newNodes.get(i).idx)){
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
                    newNodes.remove(i);
                    i--;
                }
            }
        }
        nodes = newNodes;
        trans = newTrans;

        System.out.println("Переходы в конце:");
        System.out.println(trans);
        System.out.println("Ноды в конце:");
        System.out.println(nodes);

    }
}

class Main extends JFrame {
    JPanel top, middle1, middle2, bottom;
    JButton confirm1, confirm2;
    JFormattedTextField rows, cols;
    int r, c;
    JPanel[] pans;

    JLabel info;
    JFormattedTextField[] flds;

    public static void main(String[] args){
        new Main();
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
                //flds[(i-1)*(cols+2)+j-1].setText(valueOf((i-1)*(cols+2)+j-1));
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
                confirm2.setVisible(true);
                confirm2.setEnabled(true);
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
        confirm2 = new JButton();
        confirm2.setText("Подтвердить");
        confirm2.setHorizontalAlignment(JButton.CENTER);
        confirm2.setVerticalAlignment(JButton.CENTER);
        confirm2.setVisible(false);
        confirm2.setEnabled(false);
        confirm2.addActionListener(new ActionListener() {
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
                                System.out.println(tr.getLast());
                            }
                        }
                    }
                }

                FiniteAutomation FA = new FiniteAutomation(n, tr);
                FA.Minimize(10);
                FA.ToDetermined();
            }
        });
        middle2.add(confirm2);
        middle2.setPreferredSize(new Dimension(600, 100));
        add(middle2);
        bottom = new JPanel();
        bottom.setPreferredSize(new Dimension(600, 600));
        setVisible(true);
    }
}

