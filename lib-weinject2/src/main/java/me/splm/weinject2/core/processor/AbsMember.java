package me.splm.weinject2.core.processor;

import java.util.Collection;

/**
 * Created by Administrator on 2016/8/25.
 */
public abstract class AbsMember {
    protected void add(Collection c,Object o){
        c.add(o);
    }
    protected boolean remove(Collection c,Object o){
        return c.remove(o);
    }

}
