package com.example

import com.example.core.model.RiskLevel
import com.example.domain.memory.LocalVectorStore
import com.example.domain.tools.CalculatorTool
import com.example.domain.tools.ToolInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HarmesAgentCoreTest {

    @Test
    fun testCalculatorTool_Addition() = runBlocking {
        val tool = CalculatorTool()
        val result = tool.execute(ToolInput(parameters = mapOf("expression" to "25 + 75")))
        assertTrue(result.success)
        assertEquals("100.0", result.data["result"])
    }

    @Test
    fun testCalculatorTool_Multiplication() = runBlocking {
        val tool = CalculatorTool()
        val result = tool.execute(ToolInput(parameters = mapOf("expression" to "12 * 8")))
        assertTrue(result.success)
        assertEquals("96.0", result.data["result"])
    }

    @Test
    fun testCalculatorTool_DivisionByZero() = runBlocking {
        val tool = CalculatorTool()
        val result = tool.execute(ToolInput(parameters = mapOf("expression" to "10 / 0")))
        assertTrue(!result.success || result.errorMessage != null)
    }

    @Test
    fun testVectorStore_Similarity() = runBlocking {
        val store = LocalVectorStore()
        val vec1 = store.generateLocalPseudoEmbedding("artificial intelligence agent architecture")
        val vec2 = store.generateLocalPseudoEmbedding("ai operating system agent")
        val vec3 = store.generateLocalPseudoEmbedding("cooking pasta recipe sauce")

        store.addEmbedding("doc1", "ai agent", vec1)
        store.addEmbedding("doc2", "cooking recipe", vec3)

        val searchResult = store.search(vec2, topK = 1)
        assertTrue(searchResult.isNotEmpty())
        assertEquals("doc1", searchResult.first())
    }

    @Test
    fun testCalculatorRiskLevelIsLow() {
        val tool = CalculatorTool()
        assertEquals(RiskLevel.LOW, tool.riskLevel)
    }

    @Test
    fun testNetworkModule_ProvidesMoshiAndClient() {
        val loggingInterceptor = com.example.di.NetworkModule.provideLoggingInterceptor()
        val client = com.example.di.NetworkModule.provideOkHttpClient(loggingInterceptor)
        val moshi = com.example.di.NetworkModule.provideMoshi()
        val retrofit = com.example.di.NetworkModule.provideRetrofit(client, moshi)

        assertNotNull(client)
        assertNotNull(moshi)
        assertNotNull(retrofit)
        assertEquals("https://generativelanguage.googleapis.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun testAIModule_ProvidesInferenceEngineAndProviders() {
        val cpu = com.example.domain.models.CpuBackend()
        val engine = com.example.di.AIModule.provideLocalInferenceEngine(cpu)
        val localProvider = com.example.di.AIModule.provideLocalModelProvider(engine)
        val fallbackProvider = com.example.di.AIModule.provideFallbackModelProvider()

        assertNotNull(engine)
        assertNotNull(localProvider)
        assertNotNull(fallbackProvider)
        assertEquals("Harmes Resilient Core", fallbackProvider.name)
    }
}
