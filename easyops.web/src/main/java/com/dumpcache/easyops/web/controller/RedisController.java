package com.dumpcache.easyops.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;
import com.dumpcache.easyops.redis.service.RedisClusterManager;
import com.dumpcache.easyops.redis.service.RedisClusterManager.RedisClusterNode;

/**
 * Redis管理控制台
 * 
 * @author chenke
 * @date 2016年8月18日 下午4:50:31
 */
@Controller
public class RedisController {
    @Autowired
    private RedisClusterManager redisClusterManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisController.class);

    @RequestMapping("/redis/cluster/list")
    public String clusterList(Model model) {
        List<RedisCluster> clusters = redisClusterManager.listClusters();
        model.addAttribute("clusters", clusters);
        return "redis/cluster/list";
    }

    @RequestMapping("/redis/cluster/add")
    public String addCluster() {
        return "redis/cluster/add";
    }
    
    @RequestMapping("/redis/cluster/info")
    public String infoCluster() {
        return "redis/cluster/info";
    }

    @RequestMapping("/redis/cluster/create")
    public String createCluster(@RequestParam(value = "clusterName") String clusterName,
                                @RequestParam(value = "clusterNodes") String clusterNodes,
                                Model model) {
        try {
            if (StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(clusterNodes)) {
                model.addAttribute("statusCode", 300);
                model.addAttribute("msg", "集群名称和集群节点不能为空！");
                return "error";
            }
            List<RedisClusterNode> nodes = formatClusterNodesToList(clusterNodes);
            if (nodes.size() < 3) {
                model.addAttribute("statusCode", 300);
                model.addAttribute("msg", "节点数量必须大于等于3！");
                return "error";
            }
            redisClusterManager.createRedisCluster(clusterName, nodes);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "创建集群成功！");
            return "error";
        } catch (Exception e) {
            LOGGER.error("createCluster failed:", e);
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "添加的集群节点失败，系统内部错误！");
            return "error";
        }
    }

    private List<RedisClusterNode> formatClusterNodesToList(String clusterNodes) throws Exception {
        List<RedisClusterNode> list = new ArrayList<RedisClusterNode>();
        clusterNodes = clusterNodes.replaceAll("，", ",");
        clusterNodes = clusterNodes.replaceAll("：", ":");
        String[] nodes = clusterNodes.split(",");
        if (nodes != null) {
            for (String node : nodes) {
                String[] kv = node.split(":");
                if (kv.length != 2) {
                    throw new Exception("clusterNodes 格式不正确，" + clusterNodes);
                }
                RedisClusterNode rn = new RedisClusterNode();
                rn.setHost(kv[0]);
                rn.setPort(Integer.valueOf(kv[1]));
                list.add(rn);
            }
        }
        return list;
    }

}