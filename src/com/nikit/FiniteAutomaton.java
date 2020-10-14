package com.nikit;

import java.util.HashSet;
import java.util.LinkedList;

import static java.lang.String.valueOf;

class FiniteAutomaton{
    LinkedList<Transition> trans;
    LinkedList<Node> nodes;

    /** Ввод автомата  */
    public FiniteAutomaton(LinkedList<Node> n, LinkedList<Transition> tr){
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
            // Если у какой-то ноды такое же имя, как и в переходе,
            // То заменяем ноду в переходе на ссылку на эту ноду.
            // То есть делаем граф связанным через ссылки
            for (Node node: nodes){
                if (transition.from.name.equals(node.name)){
                    transition.from = node;
                }
                for (Node n2: transition.to){
                    if (node.name.equals(n2.name)){
                        transition.to.remove(n2);
                        transition.to.add(node);
                    }
                }
            }
            if (!fl) {
                trans.add(transition);
            }
        }

        System.out.println("Переходы:");
        System.out.println(trans);
        System.out.println("Ноды:");
        System.out.println(nodes);
    }

    public Node get(Node n, String label){
        for (Transition tr: trans){
            if (tr.from.equals(n) && tr.label.equals(label)){
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
