package com.googlecode.excavator.provider.support;

import static com.googlecode.excavator.PropertyConfiger.getProviderAddress;
import static com.googlecode.excavator.PropertyConfiger.getProviderWorkers;
import static com.googlecode.excavator.PropertyConfiger.getZkConnectTimeout;
import static com.googlecode.excavator.PropertyConfiger.getZkServerList;
import static com.googlecode.excavator.PropertyConfiger.getZkSessionTimeout;

import java.net.InetSocketAddress;

import com.googlecode.excavator.Supporter;
import com.googlecode.excavator.message.Messager;

/**
 * 服务提供者支撑<br/>
 * 提供了:
 * <ul>
 * <li>WorkerSupport</li>
 * <li>ServerSupport</li>
 * <li>ServiceRegisterSupport</li>
 * </ul>
 *
 * @author vlinux
 *
 */
public class ProviderSupport implements Supporter {

    private WorkerSupport workerSupport;
    private ServerSupport serverSupport;
    private ServiceRegisterSupport serviceRegisterSupport;
    
    private final Messager messager;

    public ProviderSupport(Messager messager) {
        this.messager = messager;
    }

    @Override
    public void init() throws Exception {

        // new
        final InetSocketAddress address = getProviderAddress();
        workerSupport = new WorkerSupport(messager, getProviderWorkers());
        serverSupport = new ServerSupport(address,workerSupport);
        serviceRegisterSupport = new ServiceRegisterSupport(getZkServerList(),getZkConnectTimeout(),getZkSessionTimeout(),address,messager);

        // init
        workerSupport.init();
        serverSupport.init();
        serviceRegisterSupport.init();

    }

    @Override
    public void destroy() throws Exception {

        if (null != workerSupport) {
            workerSupport.destroy();
        }
        if (null != serverSupport) {
            serverSupport.destroy();
        }
        if (null != serviceRegisterSupport) {
            serviceRegisterSupport.destroy();
        }

    }

}
