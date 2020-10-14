package com.nikit;

/**
 * Когда разбираем синтаксис, используем токены
 * Каждый токен при этом или терминальный (+,-,*...)
 * Или не терминальный (имена переменных)
 */
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
