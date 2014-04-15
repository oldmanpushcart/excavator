package com.googlecode.excavator.test.service.impl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.common.ErrorCodeConstants;
import com.googlecode.excavator.test.common.TestException;
import com.googlecode.excavator.test.dao.TestUserDao;
import com.googlecode.excavator.test.domain.SingleResultDO;
import com.googlecode.excavator.test.domain.UserDO;
import com.googlecode.excavator.test.service.TestUserService;

public class TestUserServiceImpl implements TestUserService {

    private TestUserDao testUserDao;
    
    @Override
    public SingleResultDO<UserDO> login(String username, String password) throws TestException {
        try {
            final SingleResultDO<UserDO> result = new SingleResultDO<UserDO>();
            final Long userId = testUserDao.indexUserIdByUsername(username);
            if( null == userId ) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_USER_NOT_EXISITED, null);
                return result;
            }
            final UserDO user = testUserDao.getByUserId(userId);
            if( null == user ) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_USER_NOT_EXISITED, null);
                return result;
            }
            if( !StringUtils.equals(password, user.getPassword())) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_LOGIN_AUTH_FAILED, null);
                return result;
            }
            result.setSuccess(true);
            result.setData(user);
            return result;
        }catch(DaoException e) {
            throw new TestException("login error.", e);
        }
        
    }

    @Override
    public SingleResultDO<UserDO> getById(long userId) throws TestException {
        try {
            final SingleResultDO<UserDO> result = new SingleResultDO<UserDO>();
            final UserDO user = testUserDao.getByUserId(userId);
            if( null == user ) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_USER_NOT_EXISITED, null);
                return result;
            }
            result.setSuccess(true);
            result.setData(user);
            return result;
        }catch(DaoException e) {
            throw new TestException("getById error.", e);
        }
    }

    @Override
    public SingleResultDO<UserDO> getByUsername(String username) throws TestException {
        try {
            final SingleResultDO<UserDO> result = new SingleResultDO<UserDO>();
            final Long userId = testUserDao.indexUserIdByUsername(username);
            if( null == userId ) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_USER_NOT_EXISITED, null);
                return result;
            }
            final UserDO user = testUserDao.getByUserId(userId);
            if( null == user ) {
                result.setSuccess(false);
                result.getErrors().put(ErrorCodeConstants.ER_USER_NOT_EXISITED, null);
                return result;
            }
            result.setSuccess(true);
            result.setData(user);
            return result;
        }catch(DaoException e) {
            throw new TestException("getByUsername error.", e);
        }
    }

    @Override
    public SingleResultDO<Collection<UserDO>> searchByUsername(String username) throws TestException {
        try{
            final SingleResultDO<Collection<UserDO>> result = new SingleResultDO<Collection<UserDO>>();
            final List<UserDO> finds = testUserDao.searchByUsername(username);
            result.setSuccess(!finds.isEmpty());
            result.setData(finds);
            return result;
        }catch(DaoException e) {
            throw new TestException("getByUsername error.", e);
        }
    }

    @Override
    public SingleResultDO<Collection<UserDO>> searchByRealname(String realname) throws TestException {
        final SingleResultDO<Collection<UserDO>> result = new SingleResultDO<Collection<UserDO>>();
        try {
            final List<UserDO> finds = testUserDao.searchByRealname(realname);
            result.setSuccess(!finds.isEmpty());
            result.setData(finds);
            return result;   
        }catch(DaoException e) {
            throw new TestException("getByUsername error.", e);
        }
        
    }

    public void setTestUserDao(TestUserDao testUserDao) {
        this.testUserDao = testUserDao;
    }
    
}
