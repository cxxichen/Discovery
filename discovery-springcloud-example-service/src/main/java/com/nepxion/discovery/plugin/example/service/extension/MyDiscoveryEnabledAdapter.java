package com.nepxion.discovery.plugin.example.service.extension;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.strategy.discovery.DiscoveryEnabledAdapter;
import com.nepxion.discovery.plugin.strategy.extension.service.constant.ServiceStrategyConstant;
import com.nepxion.discovery.plugin.strategy.extension.service.context.ServiceStrategyContext;
import com.netflix.loadbalancer.Server;

public class MyDiscoveryEnabledAdapter implements DiscoveryEnabledAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MyDiscoveryEnabledAdapter.class);

    @Override
    public boolean apply(Server server, Map<String, String> metadata) {
        if (applyFromMethd(server, metadata)) {
            return applyFromHeader(server, metadata);
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean applyFromMethd(Server server, Map<String, String> metadata) {
        ServiceStrategyContext context = ServiceStrategyContext.getCurrentContext();
        Map<String, Object> attributes = context.getAttributes();

        String serviceId = server.getMetaInfo().getAppName().toLowerCase();
        String version = metadata.get(DiscoveryConstant.VERSION);

        LOG.info("Serivice端负载均衡用户定制触发：serviceId={}, host={}, metadata={}, context={}", serviceId, server.toString(), metadata, context);

        String filterServiceId = "discovery-springcloud-example-c";
        String filterVersion = "1.0";
        String filterBusinessValue = "abc";
        if (StringUtils.equals(serviceId, filterServiceId) && StringUtils.equals(version, filterVersion)) {
            if (attributes.containsKey(ServiceStrategyConstant.PARAMETER_MAP)) {
                Map<String, Object> parameterMap = (Map<String, Object>) attributes.get(ServiceStrategyConstant.PARAMETER_MAP);
                String value = parameterMap.get("value").toString();
                if (StringUtils.isNotEmpty(value) && value.contains(filterBusinessValue)) {
                    LOG.info("过滤条件：当serviceId={} && version={} && 业务参数含有'{}'的时候，不能被Ribbon负载均衡到", filterServiceId, filterVersion, filterBusinessValue);

                    return false;
                }
            }
        }

        return true;
    }

    private boolean applyFromHeader(Server server, Map<String, String> metadata) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return true;
        }

        String token = attributes.getRequest().getHeader("token");
        // String value = attributes.getRequest().getParameter("value");

        String serviceId = server.getMetaInfo().getAppName().toLowerCase();

        LOG.info("Serivice端负载均衡用户定制触发：serviceId={}, host={}, metadata={}, attributes={}", serviceId, server.toString(), metadata, attributes);

        String filterToken = "123";
        if (StringUtils.isNotEmpty(token) && token.contains(filterToken)) {
            LOG.info("过滤条件：当Token含有'{}'的时候，不能被Ribbon负载均衡到", filterToken);

            return false;
        }

        return true;
    }
}