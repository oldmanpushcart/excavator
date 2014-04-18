package com.googlecode.excavator.test.testcase;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.excavator.exception.InvokeTimeoutException;
import com.googlecode.excavator.exception.ProviderNotFoundException;
import com.googlecode.excavator.exception.ThreadPoolOverflowException;
import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.common.ErrorCodeConstants;
import com.googlecode.excavator.test.common.TestException;
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
    
    @Resource(name="testUserServiceNotFound")
    private TestUserService testUserServiceNotFound;
    
    @Resource
    private TestUserServiceImpl testUserServiceTarget;
    
    @Test
    public void test_TestUserService() {
        Assert.assertNotNull(testUserService);
        Assert.assertNotNull(testUserServiceTarget);
        Assert.assertNotNull(testUserDao);
    }
    
    /**
     * µÇÂ¼Ê§°Ü£¬µ×²ãÅ×³ö·ÇDAOÒì³£
     * @throws Exception
     */
    @Test(expected=UnsupportedOperationException.class)
    public void test_login_throw_UnsupportedOperationException() throws Exception {
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao());
            testUserService.login("username_100000", "password_200000");   
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
    }
    
    /**
     * µÇÂ¼Ê§°Ü£¬µ×²ãÅ×³öDAOÒì³£
     * @throws Exception
     */
    @Test(expected=TestException.class)
    public void test_login_throw_TestException() throws Exception {
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao(){

                @Override
                public Long indexUserIdByUsername(String username) throws DaoException {
                    throw new DaoException();
                }
                
            });
            testUserService.login("username_100000", "password_200000");   
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
    }
    
    /**
     * µÇÂ¼³É¹¦
     * @throws Exception
     */
    @Test
    public void test_login_success() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_100000", "password_100000");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
    }
    
    /**
     * µÇÂ¼Ê§°Ü£¬ÓÃ»§Ãû²»´æÔÚ
     * @throws Exception
     */
    @Test
    public void test_login_username_notfound() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_000000", "password_100000");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isSuccess());
        Assert.assertTrue(result.getErrors().containsKey(ErrorCodeConstants.ER_USER_NOT_EXISITED));
    }
    
    /**
     * µÇÂ¼Ê§°Ü£¬ÃÜÂë´íÎó
     * @throws Exception
     */
    @Test
    public void test_login_auth_failed() throws Exception {
        final SingleResultDO<UserDO> result = testUserService.login("username_100000", "password_200000");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isSuccess());
        Assert.assertTrue(result.getErrors().containsKey(ErrorCodeConstants.ER_LOGIN_AUTH_FAILED));
    }
    
    /**
     * ²¢·¢µÇÂ¼£¬µÇÂ¼³É¹¦
     * @throws Exception
     */
    @Test
    public void test_mutil_login_auth_success() throws Exception {
        final int start = 100000;
        final int end = 101000;
        final int total = end - start;
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        final AtomicInteger countDown = new AtomicInteger(total);
        final CountDownLatch countDown = new CountDownLatch(total);
        final AtomicInteger totalCounter = new AtomicInteger(0);
        final AtomicInteger successCounter = new AtomicInteger(0);
        for( int i=start; i<end; i++ ) {
            final int index = i;
            executorService.execute(new Runnable(){

                @Override
                public void run() {
                    try {
                        final String username = "username_"+index;
                        final String password = "password_"+index;
                        final SingleResultDO<UserDO> result = testUserService.login(username, password);
                        if( result.isSuccess() ) {
                            final UserDO user = result.getData();
                            if( null != user
                                    && user.getUsername().equals(username)
                                    && user.getPassword().equals(password)) {
                                successCounter.incrementAndGet();
                            }
                        }
                    } catch (TestException e) {
                        //
                    } finally {
                        totalCounter.incrementAndGet();
                        countDown.countDown();
                    }
                    
                }
                
            });
        }//for
        
        countDown.await(60, TimeUnit.SECONDS);
        Assert.assertEquals(successCounter.get(), totalCounter.get());
    }
    
    @Test(expected=InvokeTimeoutException.class)
    public void test_mutil_getById_timeout() throws Exception {
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao(){

                @Override
                public UserDO getByUserId(long userId) throws DaoException {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        //...
                    }
                    return testUserDao.getByUserId(userId);
                }
                
            });
            testUserService.getById(100000);
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
    }
    
    @Test(expected=ProviderNotFoundException.class)
    public void test_provider_notfound() throws Exception {
        testUserServiceNotFound.getById(10000);
    }
    
    @Test
    public void test_overflow_exception() throws Exception {
//        final Object lock = new Object();
        final ExecutorService executorService = Executors.newFixedThreadPool(300);
        try {
            testUserServiceTarget.setTestUserDao(new MockTestUserDao(){

                @Override
                public List<UserDO> searchByRealname(String realname) throws DaoException {
//                    synchronized (lock) {
                        try {
                            Thread.sleep(2000L);
                        } catch (InterruptedException e) {
                            //
                        }
//                    }
                    return testUserDao.searchByRealname(realname);
                }
                
            });
            int index = 0;
            int total = 300;
            
            final AtomicBoolean isThreadPoolOverflowException = new AtomicBoolean(true);
            final AtomicBoolean isIndexGt250WhenException = new AtomicBoolean(true);
            final AtomicBoolean isIndexLt250WhenNormal = new AtomicBoolean(true);
            final CountDownLatch countDown = new CountDownLatch(total);
            
                while(index++<total) {
                    
                    final int f = index;
                    executorService.execute(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                testUserService.searchByRealname("fuck");
                                if( f > 250 ) {
                                    isIndexLt250WhenNormal.set(false);
                                }
                            }catch(Throwable t) {
                                if( ! (t instanceof ThreadPoolOverflowException) ) {
                                    isThreadPoolOverflowException.set(false);
                                }
                                if( f < 250 ) {
                                    isIndexGt250WhenException.set(false);
                                }
                            } finally {
                                countDown.countDown();
                            }
                        }
                        
                    });
                    
                }
                
//                synchronized (lock) {
//                    lock.notifyAll();
//                }
                
                countDown.await(60, TimeUnit.SECONDS);
                Assert.assertTrue(isThreadPoolOverflowException.get());
                Assert.assertTrue(isIndexGt250WhenException.get());
                Assert.assertTrue(isIndexLt250WhenNormal.get());
                
        } finally {
            testUserServiceTarget.setTestUserDao(testUserDao);
        }
    }
    
}
