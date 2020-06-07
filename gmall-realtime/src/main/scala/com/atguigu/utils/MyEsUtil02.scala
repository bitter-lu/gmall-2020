package com.atguigu.utils

import java.util.Objects
import java.util

import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, BulkResult, Index}

import collection.JavaConversions._

object MyEsUtil02 {

    private val ES_HTTP_PORT = 9200
    private val ES_HOST = "http://hadoop102"
    private var factory: JestClientFactory = null

    def getClient : JestClient = {
        if (factory == null ) build()
        factory.getObject
    }

    def close(client : JestClient) = {
        if (Objects.isNull())
            client.shutdownClient()
    }

    def build() = {
        factory = new JestClientFactory
        factory.setHttpClientConfig(new HttpClientConfig.Builder(ES_HOST + ":" + ES_HTTP_PORT)
            .multiThreaded(true)
            .maxTotalConnection(200)
            .connTimeout(10000)
            .readTimeout(10000)
            .build()
        )
    }

    def insertBulk(indexName : String , docList : List[(String , Any)]) = {
        if (docList.nonEmpty) {
            val jest: JestClient = getClient
            val bulkBuilder: Bulk.Builder = new Bulk.Builder().defaultIndex(indexName).defaultType("_doc")
            for ((id , doc) <- docList) {
                val indexBuilder = new Index.Builder(doc)
                if (id != null) {
                    indexBuilder.id(id)
                }
                val index: Index = indexBuilder.build()
                bulkBuilder.addAction(index)
            }

            val bulk: Bulk = bulkBuilder.build()

            var items : util.List[BulkResult#BulkResultItem] = null

            try {
                items = jest.execute(bulkBuilder.build()).getItems
            } catch {
                case ex : Exception => println(ex.toString)
            } finally {
                close(jest)
                println("保存" + items.size() + "条数据")
                for (item <- items) {
                    if (item.error != null && item.error.nonEmpty) {
                        println(item.error)
                        println(item.errorReason)
                    }
                }
            }
        }
    }

}
