package com.googlecode.excavator.test.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.googlecode.excavator.Profiler;
import com.googlecode.excavator.test.common.DaoException;
import com.googlecode.excavator.test.dao.TestUserDao;
import com.googlecode.excavator.test.domain.UserDO;

public class TestUserDaoImpl implements TestUserDao {

    private final Map<Long,UserDO> users = new ConcurrentHashMap<Long,UserDO>();
    private final Map<String,Long> usernameIndex = new ConcurrentHashMap<String,Long>();
    private final Map<String,Long> realnameIndex = new ConcurrentHashMap<String,Long>();
    
    public TestUserDaoImpl() {
        for( long index=100000; index<=900000; index++ ) {
            UserDO user = new UserDO();
            user.setUsername("username_"+index);
            user.setPassword("password_"+index);
            user.setRealname("realname_"+index);
            user.setUserId(index);
            users.put(index, user);
            usernameIndex.put(user.getUsername(), index);
            realnameIndex.put(user.getRealname(), index);
        }
    }
    
    @Override
    public Long indexUserIdByUsername(String username) throws DaoException {
        Profiler.enter();
        try {
            return usernameIndex.get(username);
        }finally {
            Profiler.release();
        }
    }

    @Override
    public Long indexUserIdByRealname(String realname) throws DaoException {
        Profiler.enter();
        try {
            return realnameIndex.get(realname);
        }finally {
            Profiler.release();
        }
    }

    @Override
    public UserDO getByUserId(long userId) throws DaoException {
        Profiler.enter();
        try {
            return users.get(userId);
        }finally {
            Profiler.release();
        }
    }

    @Override
    public List<UserDO> searchByUsername(String username) throws DaoException {
        Profiler.enter();
        try {
            final Iterator<Entry<String, Long>> entryIt = usernameIndex.entrySet().iterator();
            final List<UserDO> finds = new ArrayList<UserDO>();
            while( entryIt.hasNext() ) {
                final Entry<String, Long> entry = entryIt.next();
                if( null == entry ) {
                    continue;
                }
                if( StringUtils.contains(entry.getKey(), username) ) {
                    final Long userId = entry.getValue();
                    if( null == userId ) {
                        continue;
                    }
                    final UserDO user = users.get(userId);
                    if( null == user ) {
                        continue;
                    }
                    finds.add(user);
                }
            }
            return finds;
        }finally {
            Profiler.release();
        }
    }

    @Override
    public List<UserDO> searchByRealname(String realname) throws DaoException {
        Profiler.enter();
        try {
            final List<UserDO> finds = new ArrayList<UserDO>();
            final Iterator<Entry<String, Long>> entryIt = realnameIndex.entrySet().iterator();
            while( entryIt.hasNext() ) {
                final Entry<String, Long> entry = entryIt.next();
                if( null == entry ) {
                    continue;
                }
                if( StringUtils.contains(entry.getKey(), realname) ) {
                    final Long userId = entry.getValue();
                    if( null == userId ) {
                        continue;
                    }
                    final UserDO user = users.get(userId);
                    if( null == user ) {
                        continue;
                    }
                    finds.add(user);
                }
            }
            return finds;
        }finally {
            Profiler.release();
        }
    }

}
