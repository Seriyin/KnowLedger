package org.knowledger.agent.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.util.KtorExperimentalAPI
import org.knowledger.agent.data.DataSource
import org.knowledger.agent.data.apis.ApiAdapter
import org.tinylog.kotlin.Logger
import kotlin.collections.set
import kotlin.random.Random

class HTTPRunner {

    @KtorExperimentalAPI
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
        }
    }

    private val apis: MutableList<DataSource> = mutableListOf()
    private val matchers: MutableMap<String, ApiAdapter> = mutableMapOf()


    fun registerMatcher(toMatch: String, toDeserialize: ApiAdapter) {
        matchers[toMatch] = toDeserialize
    }

    fun registerMatchers(matchers: Map<String, ApiAdapter>) {
        this.matchers.putAll(matchers)
    }


    fun registerSources(apis: List<DataSource>) {
        this.apis.addAll(apis)
    }

    suspend fun runRandom() =
        runApiQuery(Random.nextInt(apis.size))


    fun registeredApis(): Int = apis.size

    suspend fun run(i: Int) =
        if (i < registeredApis() && i > 0) {
            runApiQuery(i)
        } else {
            Logger.error(IndexOutOfBoundsException())
            null
        }

    private suspend fun runApiQuery(i: Int) =
        matchers[apis[i].id]?.query(client, apis[i])
}
