package com.googlecode.excavator.test.testcase;

import junit.framework.Assert;

import org.junit.Test;

import com.googlecode.excavator.serializer.HessianSerializer;
import com.googlecode.excavator.serializer.JavaSerializer;
import com.googlecode.excavator.serializer.Serializer;

public class SerializerTestCase {

    @Test
    public void test_for_java_serializer() throws Exception {
        do_test_for_serializer(new JavaSerializer());
    }
    
    @Test
    public void test_for_hessian_serializer() throws Exception {
        do_test_for_serializer(new HessianSerializer());
    }
    
    private void do_test_for_serializer(final Serializer serializer) throws Exception {
        Assert.assertNotNull(serializer);
        final String words = "abcdefghijk";
        final byte[] datas = serializer.encode(words);
        final String obj = serializer.decode(datas);
        Assert.assertEquals(words, obj);
    }
    
}
