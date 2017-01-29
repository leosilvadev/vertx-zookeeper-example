package io.vertx.example.zookeeper.discovery

import io.vertx.core.Vertx
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.HttpEndpoint

/**
 * Created by leonardo on 1/23/17.
 */
class Application {

  static main(args) {
    Vertx vertx = Vertx.vertx()
    def discovery = ServiceDiscovery.create(vertx, [
        backendConfiguration: [
            connection: "127.0.0.1:2181"
        ]
    ])

    def record = HttpEndpoint.createRecord("get-transactions", "localhost")
    discovery.publish(record, {
      discovery.getRecords({ true }, {
        println "After"
        println it.result()
      })
    })

    discovery.getRecord({ true }, {
      println "Before"
      println it.result()
    })

    sleep(5000)
  }

}
