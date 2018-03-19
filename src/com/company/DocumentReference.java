package com.company;

import java.io.File;

public class DocumentReference {

    public File file = null;
    public double length = 0;


    public DocumentReference(File file,double length){
        this.file = file;
        this.length = length;
    }
}
