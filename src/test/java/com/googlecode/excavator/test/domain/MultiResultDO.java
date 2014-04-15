package com.googlecode.excavator.test.domain;

import java.util.HashMap;
import java.util.Map;

public class MultiResultDO<K,M> extends ResultDO {

    private static final long serialVersionUID = 9060393979619640004L;
    
    private Map<K,M> datas = new HashMap<K,M>();
    private Map<K,Map<String,String>> errors = new HashMap<K,Map<String,String>>();
    
    public Map<K, M> getDatas() {
        return datas;
    }
    public void setDatas(Map<K, M> datas) {
        this.datas = datas;
    }
    public Map<K, Map<String, String>> getErrors() {
        return errors;
    }
    public void setErrors(Map<K, Map<String, String>> errors) {
        this.errors = errors;
    }
    
    public void putError(K key, String error, String reason) {
        Map<String,String> subErrors = errors.get(key);
        if( null == subErrors ) {
            subErrors = new HashMap<String,String>();
            errors.put(key, subErrors);
        }
        subErrors.put(error, reason);
    }

}
