package io.vertx.example.zookeeper.cluster

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.CuratorEvent
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.curator.framework.recipes.cache.NodeCacheListener
import org.apache.zookeeper.CreateMode

/**
 * Created by leonardo on 1/23/17.
 */
class Application {

  static main(args) {
    VertxOptions options1 = new VertxOptions().setClusterManager(new ZookeeperClusterManager())
    Vertx.clusteredVertx(options1, { res ->
      if (res.succeeded()) {
        def vertx = res.result()
        vertx.eventBus().consumer('myChannelName', {
          println "Received: ${it.body()}"
        })

      } else {
        // failed!
      }
    })


    VertxOptions options2 = new VertxOptions().setClusterManager(new ZookeeperClusterManager())
    Vertx.clusteredVertx(options2, { res ->
      if (res.succeeded()) {
        def vertx = res.result()
        def num = 0
        vertx.setPeriodic(1000, {
          num++
          vertx.eventBus().send('myChannelName', [counter: num])
        })

      } else {
        // failed!
      }
    })
  }

}
