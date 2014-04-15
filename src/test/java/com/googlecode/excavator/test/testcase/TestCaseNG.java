package com.googlecode.excavator.test.testcase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:spring/applicationContext.xml"})
public class TestCaseNG {
    
    @Test
    public void _test() {
        Assert.assertTrue(true);
    }
    
}
