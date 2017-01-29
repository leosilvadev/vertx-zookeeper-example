package io.vertx.example.zookeeper.config_management

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

  static void setupConfig(ZookeeperClusterManager mgr, String envName) {
    def path = "/syncMap/config/$envName"
    def exist = mgr.curatorFramework.checkExists().forPath(path)
    if (!exist) {
      mgr.curatorFramework
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .forPath(path)
    }
  }

  static main(args) {
    ZookeeperClusterManager mgr = new ZookeeperClusterManager()
    VertxOptions options = new VertxOptions().setClusterManager(mgr)
    Vertx.clusteredVertx(options, { res ->
      if (res.succeeded()) {
        def vertx = res.result()

        setupConfig(mgr, 'dev')

        mgr.curatorFramework.getCuratorListenable().addListener({ CuratorFramework client, CuratorEvent event ->
          println 'CHANGED!!'
          println event
          println 'CHANGED!!'
        })

        mgr.curatorFramework.setData()
            .forPath('/syncMap/config/dev', JsonOutput.toJson([name: 'Leonardo', age: 27]).bytes)

        def cache = new NodeCache(mgr.curatorFramework, '/syncMap/config/dev')
        cache.start()
        println cache.currentData
        cache.listenable.addListener(new NodeCacheListener() {
          @Override
          void nodeChanged() throws Exception {
            println Thread.currentThread().name
            println new JsonSlurper().parse(cache.currentData.data)
          }
        })

        def c = 2000
        vertx.setPeriodic(10000, {
          println("Sending...: ${Thread.currentThread().name}")
          c++
          mgr.curatorFramework.setData()
              .forPath('/syncMap/config/dev', JsonOutput.toJson([name: 'Leonardo', age: c]).bytes)
        })
        println("Finishing: ${Thread.currentThread().name}")
      } else {
        // failed!
      }
    })
  }

}
