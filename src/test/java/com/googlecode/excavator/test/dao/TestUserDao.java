package com.googlecode.excavator.test.dao;

import java.util.List;

import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.domain.UserDO;

public interface TestUserDao {

    Long indexUserIdByUsername(String username) throws DaoException;
    Long indexUserIdByRealname(String realname) throws DaoException;
    List<UserDO> searchByUsername(String username) throws DaoException;
    List<UserDO> searchByRealname(String realname) throws DaoException;
    UserDO getByUserId(long userId) throws DaoException;
    
}
