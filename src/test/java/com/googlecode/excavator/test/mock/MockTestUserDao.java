package com.googlecode.excavator.test.mock;

import java.util.List;

import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.dao.TestUserDao;
import com.googlecode.excavator.test.domain.UserDO;

public class MockTestUserDao implements TestUserDao {

    @Override
    public Long indexUserIdByUsername(String username) throws DaoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long indexUserIdByRealname(String realname) throws DaoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UserDO> searchByUsername(String username) throws DaoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UserDO> searchByRealname(String realname) throws DaoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserDO getByUserId(long userId) throws DaoException {
        throw new UnsupportedOperationException();
    }

}
