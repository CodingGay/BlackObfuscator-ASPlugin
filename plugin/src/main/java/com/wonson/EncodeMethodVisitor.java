package com.wonson;
import org.objectweb.asm.MethodVisitor;
import java.util.Random;
public abstract class EncodeMethodVisitor extends MethodVisitor{
    protected boolean on;
    protected Random random;
    protected String owner;
    protected String data_name;
    protected String index_name;

    public void setOn(boolean on) {
        this.on = on;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setData_name(String data_name) {
        this.data_name = data_name;
    }

    public void setIndex_name(String index_name) {
        this.index_name = index_name;
    }

    public EncodeMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

}
