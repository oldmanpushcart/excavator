package com.googlecode.excavator.test.domain;

import java.io.Serializable;

public abstract class ResultDO implements Serializable {

    private static final long serialVersionUID = -1369434131311843294L;
    
    private boolean isSuccess;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    
}
