package com.lastlight.common;

public enum RegularConstant {
    REG_NULL(""),
    REG_PASSWORD("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[\\w!@#$%^&*()_+|~\\-=`{}\\[\\]:\";'<>?,./]{8,16}$"),
    REG_EMAIL("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    String reg;

    RegularConstant(String reg) {
        this.reg = reg;
    }

    public String getReg() {
        return reg;
    }
}
