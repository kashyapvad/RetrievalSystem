package com.company;

public class TokenOccurrence {

    public DocumentReference documentReference = null;
    public int count = 0;

    public TokenOccurrence(DocumentReference documentReference, int count){
        this.documentReference = documentReference;
        this.count = count;
    }
}
