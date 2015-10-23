package com.redis.messaging

import java.util.Properties
import java.util.stream.Collectors
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction
import redis.clients.jedis.Pipeline
import redis.clients.jedis.exceptions._
import scala.collection.JavaConversions._



object MessageConsumer extends Serializable {

   @transient private var pool: JedisPool = null

   def createPool(redisHost: String, redisPort: Int, redisTimeout: Int) {
      if (pool == null) {
         val poolConfig = new JedisPoolConfig()
   //      poolConfig.setMaxWait(redisTimeout)
         poolConfig.setMaxIdle(5)
         poolConfig.setMinIdle(1)
         poolConfig.setTestOnBorrow(true)

         pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout, "Fg{Xxjii29458jpasswordN")
         
         val hook = new Thread {
            override def run = pool.destroy() 
         }
         sys.addShutdownHook(hook.run)
      }
   }

   def getPool: JedisPool = {
      assert(pool != null)
      pool
   }


   def main(args: Array[String]): Unit = {

      val redisHost = "localhost"
      val redisPort = 6379
      val redisTimeout = 2000
      val msgQueue = "delay-queue"
      val msgHashKey = "delay-key"
      val delay = 5

      createPool(redisHost, redisPort, redisTimeout)

      val jedis = getPool.getResource
     // jedis.auth("Fg{Xxjii29458jpasswordN")

      try {
        while(true) {
           processMessages(jedis, msgQueue, msgHashKey, delay)
           Thread.sleep(10000L)
        }
      } finally {
         getPool.returnResource(jedis)
      }
    
   }


   def processMessages(jedis: Jedis, msgQueue: String, msgHashKey: String, delay: Int) {
 
      def startTime = 0
      def endTime = System.currentTimeMillis() / 1000 - delay

      val p = jedis.pipelined()
      p.multi()
      val response = p.zrangeByScore(msgQueue, startTime, endTime)
      p.zremrangeByScore(msgQueue, startTime, endTime)
      p.exec()

      // Close the multi block before running the commands below
      p.syncAndReturnAll()


      val keys = response.get()
      val keyList = keys.stream().collect(Collectors.toList())

  //    def tMessage = jedis.multi()
  //    def msgResponse = tMessage.hmget(msgHashKey, keyString)
  //    tMessage.hdel(msgHashKey, keyString)
  //    tMessage.exec()

      if (keyList.size() == 0) {
         println("No keys need to be processed.")
         return
      }


      p.multi()
      val msgResponse = p.hmget(msgHashKey, keyList: _*)
      p.hdel(msgHashKey, keyList: _*)
      p.exec()
      p.syncAndReturnAll()

      val messages = msgResponse.get()
      if (messages.size() != 0) {
         (keys.toList zip messages.toList).toList.foreach {  case (key, message) =>
            println("Message for key: " + key + " is " + message )
         }
      }
      else {
         println("message is null")
      }
   }

}

