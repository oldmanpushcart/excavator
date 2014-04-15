package com.googlecode.excavator.test.domain;

import java.util.HashMap;
import java.util.Map;

public class SingleResultDO<M> extends ResultDO {

    private static final long serialVersionUID = -6693531722607357898L;
    
    private M data;
    private Map<String,String> errors = new HashMap<String,String>();

    public M getData() {
        return data;
    }

    public void setData(M data) {
        this.data = data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

}
