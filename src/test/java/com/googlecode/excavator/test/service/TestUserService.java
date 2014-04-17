package com.googlecode.excavator.test.service;

import java.util.Collection;

import com.googlecode.excavator.test.common.TestException;
import com.googlecode.excavator.test.domain.SingleResultDO;
import com.googlecode.excavator.test.domain.UserDO;

public interface TestUserService {

    SingleResultDO<UserDO> login(String username, String password) throws TestException;
    SingleResultDO<UserDO> getById(long userId) throws TestException;
    SingleResultDO<UserDO> getByUsername(String username) throws TestException;
    SingleResultDO<Collection<UserDO>> searchByUsername(String username) throws TestException;
    SingleResultDO<Collection<UserDO>> searchByRealname(String realname) throws TestException;
    
}
