package volthack.util.github

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class GitHubClient(private val repo: String) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val gson = Gson()

    fun getLatestRelease(): Release? {
        val json = fetch("releases/latest") ?: return null
        val obj = gson.fromJson(json, JsonObject::class.java)
        return Release(
            tag = obj.get("tag_name")?.asString ?: return null,
            name = obj.get("name")?.asString ?: "",
            body = obj.get("body")?.asString ?: "",
            url = obj.get("html_url")?.asString ?: ""
        )
    }

    fun checkUpdate(currentVersion: String): Release? {
        val release = getLatestRelease() ?: return null
        val tagVersion = release.tag.removePrefix("v")
        return if (tagVersion > currentVersion) release else null
    }

    private fun fetch(path: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$repo/$path"))
            .header("Accept", "application/vnd.github.v3+json")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return if (response.statusCode() == 200) response.body() else null
    }

    data class Release(
        val tag: String,
        val name: String,
        val body: String,
        val url: String
    )
}
