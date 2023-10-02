package io.ipfs.kotlin

import io.ipfs.kotlin.defaults.localIPFSConfig
import okhttp3.mockwebserver.MockWebServer
import okio.fakefilesystem.FakeFileSystem
import org.junit.After
import org.junit.Before

abstract class BaseIPFSWebserverTest {

    val server = MockWebServer()
    val fileSystem = FakeFileSystem()

    val ipfs: IPFS by lazy {
        IPFS(localIPFSConfig.copy(base_url = server.url("").toString(), fileSystem = fileSystem))
    }

    @Before
    fun runBeforeEveryTest() {
        server.start()

    }

    @After
    fun runAfterEveryTest() {
        server.shutdown()
    }
}