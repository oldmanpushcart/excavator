package com.googlecode.excavator.test.testcase;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.excavator.Runtimes;
import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.dao.TestUserDao;
import com.googlecode.excavator.test.domain.SingleResultDO;
import com.googlecode.excavator.test.domain.UserDO;
import com.googlecode.excavator.test.mock.MockTestUserDao;
import com.googlecode.excavator.test.service.TestUserService;
import com.googlecode.excavator.test.service.impl.TestUserServiceImpl;

public class RuntimesTestCase extends TestCaseNG {

    @Resource(name="testUserService")
    private TestUserService testUserService;
    
    @Resource
    private TestUserServiceImpl testUserServiceTarget;
    
    @Resource
    private TestUserDao testUserDao;
    
    @Test
    public void test_login_runtimes() throws Exception {
        
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao(){

                @Override
                public UserDO getByUserId(long userId) throws DaoException {
                    final Runtimes.Runtime runtime = Runtimes.getRuntime();
                    Assert.assertEquals("excavator_test", runtime.getConsumer());
                    Assert.assertEquals("excavator_test", runtime.getProvider());
                    Assert.assertEquals(TestUserService.class, runtime.getServiceInterface());
                    
                    Assert.assertNotNull(runtime.getConsumerAddress());
                    Assert.assertNotNull(runtime.getProviderAddress());
                    
                    Assert.assertNotNull(runtime.getReq());
                    Assert.assertNotNull(runtime.getReq().getAppName());
                    Assert.assertNotNull(runtime.getReq().getGroup());
                    Assert.assertNotNull(runtime.getReq().getKey());
                    Assert.assertNotNull(runtime.getReq().getSign());
                    Assert.assertNotNull(runtime.getReq().getToken());
                    Assert.assertNotNull(runtime.getReq().getVersion());
                    Assert.assertNotNull(runtime.getReq().getTimeout());
                    Assert.assertNotNull(runtime.getReq().getTimestamp());
                    Assert.assertNotNull(runtime.getReq().getArgs());
                    Assert.assertEquals(2, runtime.getReq().getArgs().length);
                    Assert.assertEquals("username_100000", runtime.getReq().getArgs()[0]);
                    Assert.assertEquals("password_200000", runtime.getReq().getArgs()[1]);
                    return testUserDao.getByUserId(userId);
                }

                @Override
                public Long indexUserIdByUsername(String username) throws DaoException {
                    return testUserDao.indexUserIdByUsername(username);
                }
                
            });
            testUserService.login("username_100000", "password_200000");   
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
        
        final SingleResultDO<UserDO> result = testUserService.login("username_100000", "password_100000");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
    }
    
}
