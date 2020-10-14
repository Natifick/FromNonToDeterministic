package com.nikit;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * После завершения действий над автоматом нужно его показать
 * Удобнее делать это в новом окне
 */
class NewWindow extends JFrame {
    JLabel[] flds;
    final String REPLACE_VALUE = "";
    void print_automaton(FiniteAutomaton FA){
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

        for (Transition transition : tr) {
            int indx = nodes.indexOf(transition.from);
            flds[(indx+1)* cols].setText(transition.from.name);
            flds[(indx+1)*cols + 1].setText(transition.from.fin ? "1" : "0");
            for (Node t2 : transition.to) {
                if (flds[(indx+1)*cols + labels.indexOf(transition.label)+2]==null){
                    flds[(indx+1)*cols + labels.indexOf(transition.label)+2] = new JLabel();
                    flds[(indx+1)*cols + labels.indexOf(transition.label)+2].setText("");
                }
                else{
                    flds[(indx+1)*cols + labels.indexOf(transition.label)+2].setText(flds[(indx+1)*cols + labels.indexOf(transition.label)+2].getText() + " " + t2.name);
                }
            }
        }

        for (int i=1;i<rows;i++){
            // TODO добавить все невошедшие в список ноды (в колонке слева, они там пустыми полями остаются)
            for (int j=2;j<cols;j++){
                if (flds[i*cols+j]==null){
                    flds[i*cols+j] = new JLabel();
                    flds[i*cols+j].setText("");
                }
            }
        }
        for (JLabel lb : flds) {
            add(lb);
        }
        setVisible(true);
    }
}
