package com.company;

import java.util.ArrayList;

public class TokenInfo {

    public double idf;
    public ArrayList<TokenOccurrence> occList;

    public TokenInfo(){
        occList = new ArrayList();
        idf =0;
    }
}
