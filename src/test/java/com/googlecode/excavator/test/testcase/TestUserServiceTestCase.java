package com.googlecode.excavator.test.testcase;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.excavator.test.common.ErrorCodeConstants;
import com.googlecode.excavator.test.dao.TestUserDao;
import com.googlecode.excavator.test.domain.SingleResultDO;
import com.googlecode.excavator.test.domain.UserDO;
import com.googlecode.excavator.test.mock.MockTestUserDao;
import com.googlecode.excavator.test.service.TestUserService;
import com.googlecode.excavator.test.service.impl.TestUserServiceImpl;

public class TestUserServiceTestCase extends TestCaseNG {

    @Resource
    private TestUserDao testUserDao;
    
    @Resource(name="testUserService")
    private TestUserService testUserService;
    
    @Resource
    private TestUserServiceImpl testUserServiceTarget;
    
    @Test
    public void test_TestUserService() {
        Assert.assertNotNull(testUserService);
        Assert.assertNotNull(testUserServiceTarget);
        Assert.assertNotNull(testUserDao);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void test_login_throw_exception() throws Exception {
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao());
            testUserService.login("username_100000", "password_200000");   
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
    }
    
    @Test
    public void test_login_success() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_100000", "password_100000");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
    }
    
    @Test
    public void test_login_username_notfound() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_000000", "password_100000");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isSuccess());
        Assert.assertTrue(result.getErrors().containsKey(ErrorCodeConstants.ER_USER_NOT_EXISITED));
    }
    
    @Test
    public void test_login_auth_failed() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_100000", "password_200000");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isSuccess());
        Assert.assertTrue(result.getErrors().containsKey(ErrorCodeConstants.ER_LOGIN_AUTH_FAILED));
    }
    
}
