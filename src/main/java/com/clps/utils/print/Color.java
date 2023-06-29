package com.clps.utils.print;

public enum Color {
    Black("Black", "30"),
    Red("Red", "31"),
    Green("Green", "32"),
    Yellow("Yellow", "33"),
    Blue("Blue", "34"),
    Purple("Purple", "35"),
    DeepGreen("DeepGreen", "36"),
    White("White", "37");

    Color(String name, String value) {
        _name = name;
        _value = value;
    }

    private String _name;
    private String _value;

    public static String getCode(Color color) {
        return color._value;
    }

    public String getCode(){
        return _value;
    }
}