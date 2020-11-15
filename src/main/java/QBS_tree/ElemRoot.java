package QBS_tree;

import geometry.*;

import java.io.Serializable;

public class ElemRoot extends Point implements Cloneable ,Serializable {

    public ElemRoot(){}

    public ElemRoot(double[] data) {
        super(data);
    }


    @Override
    public ElemRoot clone() {
        return (ElemRoot) super.clone();
    }


}
