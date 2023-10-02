package io.ipfs.kotlin

import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okio.Path.Companion.toPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TestAdd : BaseIPFSWebserverTest() {

    @Test
    fun testAddString() = runTest {
        // setup
        server.enqueue(
            MockResponse().setHeader("Content-Type", ContentType.Application.Json)
                .setBody("""{"Hash":"hashprobe","Name":"nameprobe", "Size":"1"}""")
        )

        // invoke
        val addString = ipfs.add.string("foo")

        // assert
        assertThat(addString.hash).isEqualTo("hashprobe")
        assertThat(addString.name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest();
        assertThat(executedRequest.path).startsWith("/add")

    }

    @Test
    fun testAddFile() = runTest {
        // setup
        server.enqueue(
            MockResponse().setHeader("Content-Type", ContentType.Application.Json)
                .setBody("""{"Hash":"hashprobe","Name":"nameprobe", "Size":"1"}""")
        )

        val tempTestFile = "temptestfile".toPath()
        fileSystem.write(tempTestFile) { writeUtf8("") }
        // invoke
        val addString = ipfs.add.file(tempTestFile)

        // assert
        assertThat(addString.hash).isEqualTo("hashprobe")
        assertThat(addString.name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest();
        assertThat(executedRequest.path).startsWith("/add");

    }

    @Test
    fun testAddDirectory() = runTest {
        // setup
        server.enqueue(
            MockResponse().setHeader("Content-Type", ContentType.Application.Json)
                .setBody("""{"Hash":"hashprobe","Name":"nameprobe", "Size":"1"}""")
        );

        // create nested subdirectories
        val path = "temptestdir".toPath()
        fileSystem.createDirectory(path)
        val dttf = path.resolve("dirtemptestfile")
        fileSystem.write(dttf) { writeUtf8("Contents of temptestdir/dirtemptestfile") }
        val subdirpath = path.resolve("subdir")
        fileSystem.createDirectory(subdirpath)
        val sdttf = subdirpath.resolve("subdirtemptestfile")
        fileSystem.write(sdttf) { writeUtf8("Contents of temptestdir/subdir/subdirtemptestfile") }
        val dttf2 = path.resolve("dirtemptestfile2")
        fileSystem.write(dttf2) { writeUtf8("Contents of temptestdir/dirtemptestfile2") }

        val result = ipfs.add.directory(path, path.name)

        // assert
        assertThat(result.first().hash).isEqualTo("hashprobe")
        assertThat(result.first().name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest()
        val body = executedRequest.body.readUtf8()
        assertThat(executedRequest.path).startsWith("/add")
        assertThat(body).containsPattern(""".*temptestdir.*""")
    }


}
