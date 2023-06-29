package com.clps.utils.print;

public enum BGColor {
    Black("Black", "40"),
    DeepRed("DeepRed", "41"),
    Green("Green", "42"),
    Yellow("Yellow", "43"),
    Blue("Blue", "44"),
    Purple("Purple", "45"),
    DeepGreen("DeepGreen", "46"),
    White("White", "47");

    BGColor(String name, String value) {
        _name = name;
        _value = value;
    }

    private String _name;
    private String _value;

    public static String getCode(BGColor bgColor) {
        return bgColor._value;
    }

    public String getCode(){
        return _value;
    }
}
