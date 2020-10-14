package com.nikit;

import java.util.*;
import static java.lang.String.valueOf;

/*
To test it:
S ::= A;
A ::= E | +E | A+E | A*E
E ::= id | (A)
over
y

*/

/**
 * главный класс, который принимает на вход LR грамматику в форме Бекуса-Науера
 * И составляет конечный автомат по ней
 */
class Main{
    static LinkedList<Node> nodes;
    static LinkedList<Transition> trans;
    public static void main(String[] args){
        LinkedList<String> firsta, second;
        firsta = new LinkedList<>();
        second = new LinkedList<>();
        firsta.add("1");
        firsta.add("3");
        firsta.add("2");
        second.add("1");
        second.add("2");
        second.add("3");
        System.out.println(firsta.equals(second));


        Scanner sc = new Scanner(System.in);
        LinkedList<String> names = new LinkedList<>();
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
        nodes = new LinkedList<>();
        trans = new LinkedList<>();
        for (int i=0;i<tokens.size();i++){
            for (int j=1;j<tokens.get(i).size();j++){
                // нашим лейблом будет имя текущего токена, переход из позиции с номером j в j+1
                trans.add(new Transition(tokens.get(i).get(j).name, new Node(valueOf(j-1) + "," + valueOf(i)), new Node(valueOf(j) + "," + valueOf(i))));
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

        NewWindow f = new NewWindow();

        System.out.println("Детерминировать автомат?[y/n]");
        if (sc.nextLine().equals("y")){
            FiniteAutomaton fa = new FiniteAutomaton(nodes, trans);
            fa.ToDetermined();
            f.print_automaton(fa);
        }
        else{
            f.print_automaton(new FiniteAutomaton(nodes, trans));
        }
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
                NewWindow fr = new NewWindow();
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
                NewWindow fr = new NewWindow();
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

