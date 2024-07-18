package com.mcsirius.cloud.route;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Component
@Slf4j
@AllArgsConstructor
public class DynamicRouteLoader {
    private final NacosConfigManager nacosConfigManager;
    private final String dataId = "cloud-gateway.json";
    private final String group = "DEFAULT_GROUP";

    /** 已加载的路由id集合 */
    private static final List<String> ROUTE_LIST = new ArrayList<>();

    private final RouteDefinitionWriter routeDefinitionWriter;

    /** 事件发布器 */
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void initRouteListener() throws NacosException {
        // 1. 项目启动时，先拉取一次配置，并添加监听器
        String configInfo =nacosConfigManager.getConfigService().getConfigAndSignListener(dataId, group, 5000, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                // 2. 监听配置变更时，更新路由表
                updateConfigInfo(configInfo);
            }
        });
        // 3. 第一次拉取配置时，更新路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo (String configInfo) {
        clearRoute();
        // 解析从Nacos配置中读取的路由配置信息
        List<RouteDefinition> gatewayRouteDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        for (RouteDefinition routeDefinition : gatewayRouteDefinitions) {
            // 将路由写到定义器中
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            // 将路由id加入内存集合中
            ROUTE_LIST.add(routeDefinition.getId());
        }

        // 刷新路由定义器
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this.routeDefinitionWriter));
    }

    /**
     * 方法描述： 清空已存在的路由
     */
    private void clearRoute() {
        ROUTE_LIST.forEach(id -> this.routeDefinitionWriter.delete(Mono.just(id)).subscribe());
        ROUTE_LIST.clear();
    }
}
