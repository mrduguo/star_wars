#!/usr/bin/env kscript

@file:DependsOn("com.github.kittinunf.fuel:fuel-gson:2.0.1")

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.gson.responseObject
import java.net.URLEncoder
import kotlin.system.exitProcess


/*
     _______.___________.    ___      .______         ____    __    ____  ___      .______          _______.
    /       |           |   /   \     |   _  \        \   \  /  \  /   / /   \     |   _  \        /       |
   |   (----`---|  |----`  /  ^  \    |  |_)  |        \   \/    \/   / /  ^  \    |  |_)  |      |   (----`
    \   \       |  |      /  /_\  \   |      /          \            / /  /_\  \   |      /        \   \
.----)   |      |  |     /  _____  \  |  |\  \----.      \    /\    / /  _____  \  |  |\  \----.----)   |
|_______/       |__|    /__/     \__\ | _| `._____|       \__/  \__/ /__/     \__\ | _| `._____|_______/

 */

when {
    args.isEmpty() -> showUsage()
    args[0] == "TEST" -> executeTests()
    else -> executeSearch()
}

/*
 _______       ___   .___________.    ___         .___  ___.   ______    _______   _______  __
|       \     /   \  |           |   /   \        |   \/   |  /  __  \  |       \ |   ____||  |
|  .--.  |   /  ^  \ `---|  |----`  /  ^  \       |  \  /  | |  |  |  | |  .--.  ||  |__   |  |
|  |  |  |  /  /_\  \    |  |      /  /_\  \      |  |\/|  | |  |  |  | |  |  |  ||   __|  |  |
|  '--'  | /  _____  \   |  |     /  _____  \     |  |  |  | |  `--'  | |  '--'  ||  |____ |  `----.
|_______/ /__/     \__\  |__|    /__/     \__\    |__|  |__|  \______/  |_______/ |_______||_______|

 */

data class StarWarCharacter(
        var name: String,
        var species: List<String>,
        var homeworld: String,
        var films: List<String>

) {
    override fun toString(): String {
        return """
            * Name: $name
            * Species: ${species.joinToString(", ")}
            * Home World: $homeworld
            * Movies: ${films.joinToString(", ")}
            """.trimIndent()
    }
}

data class StarWarSpecies(
        var name: String,
        var average_lifespan: String
) {
    override fun toString(): String {
        return "$name (Average Lifespan: $average_lifespan years)"
    }
}

data class StarWarHomeWorld(var name: String)

data class StarWarFilm(var title: String)

data class SearchResult(
        var count: Int,
        var next: String,
        val results: List<StarWarCharacter>
)


/*
     ___      .______    __       ______  __       __   _______ .__   __. .___________.
    /   \     |   _  \  |  |     /      ||  |     |  | |   ____||  \ |  | |           |
   /  ^  \    |  |_)  | |  |    |  ,----'|  |     |  | |  |__   |   \|  | `---|  |----`
  /  /_\  \   |   ___/  |  |    |  |     |  |     |  | |   __|  |  . `  |     |  |
 /  _____  \  |  |      |  |    |  `----.|  `----.|  | |  |____ |  |\   |     |  |
/__/     \__\ | _|      |__|     \______||_______||__| |_______||__| \__|     |__|


 */

class StarWarsApiClient {
    val baseUrl = System.getenv("STAR_WARS_API_BASE_URL") ?: "https://swapi.co/api"
    val searchMaxResultCount = (System.getenv("STAR_WARS_API_SEARCH_MAX_RESULT") ?: "10").toInt()

    fun searchPeopleByTerm(searchTerm: String): List<StarWarCharacter> {
        val resultList = mutableListOf<StarWarCharacter>()
        var searchUrl = "$baseUrl/people/?search=${URLEncoder.encode(searchTerm, "UTF-8")}"
        loop@while (true) {
            val searchResult = executeGetRequest<SearchResult>(searchUrl)
            for (character in searchResult.results) {
                resultList.add(character)
                if (resultList.size == searchMaxResultCount) {
                    break@loop
                }
            }
            if (searchResult.next != null) {
                searchUrl = searchResult.next
            } else {
                break@loop
            }
        }
        return resultList
    }


    inline fun <reified T : Any> executeGetRequest(url: String): T {
        val (request, response, result) = Fuel.get(url)
                .header(Headers.USER_AGENT, "curl/7.54.0")
                .header(Headers.ACCEPT, "*/*")
                .responseObject<T>()

        if (response.isSuccessful) {
            return result.get()
        } else {
            throw Exception("Failed to execute request with response: ${response}")
        }
    }
}


/*
     _______.  ______    __       __    __  .___________. __    ______   .__   __.
    /       | /  __  \  |  |     |  |  |  | |           ||  |  /  __  \  |  \ |  |
   |   (----`|  |  |  | |  |     |  |  |  | `---|  |----`|  | |  |  |  | |   \|  |
    \   \    |  |  |  | |  |     |  |  |  |     |  |     |  | |  |  |  | |  . `  |
.----)   |   |  `--'  | |  `----.|  `--'  |     |  |     |  | |  `--'  | |  |\   |
|_______/     \______/  |_______| \______/      |__|     |__|  \______/  |__| \__|

 */


fun showUsage() {
    println("Usage:\n  ./star_wars <search term>")
    exitProcess(-1)
}

fun executeSearch() {
    try {
        val characters = searchStarWarCharacters(args.joinToString(" "))
        if (characters.isNotEmpty()) {
            println(characters.joinToString("\n========================================================\n"))
        } else {
            println("error: no character found for search term <${args.joinToString(" ")}>")
            exitProcess(-1)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(-1)
    }
}

fun searchStarWarCharacters(searchTerm: String): List<StarWarCharacter> {
    val starWarApiClient = StarWarsApiClient()
    val characters = starWarApiClient.searchPeopleByTerm(searchTerm)
    characters.forEach { enrichCharacter(starWarApiClient, it) }
    return characters
}

fun enrichCharacter(starWarsApiClient: StarWarsApiClient, character: StarWarCharacter) {
    character.homeworld = starWarsApiClient.executeGetRequest<StarWarHomeWorld>(character.homeworld).name
    character.species = character.species.map { species ->
        starWarsApiClient.executeGetRequest<StarWarSpecies>(species).toString()
    }
    character.films = character.films.map { film ->
        starWarsApiClient.executeGetRequest<StarWarFilm>(film).title
    }
}

/*
.___________. _______     _______.___________.    _______.
|           ||   ____|   /       |           |   /       |
`---|  |----`|  |__     |   (----`---|  |----`  |   (----`
    |  |     |   __|     \   \       |  |        \   \
    |  |     |  |____.----)   |      |  |    .----)   |
    |__|     |_______|_______/       |__|    |_______/
 */


fun executeTests() {
    try {
        testExistCharacter()
        testNoneExistCharacter()
        testMultiPageSearch()
        testInvalidApiRequest()
    } catch (e: Exception) {
        e.printStackTrace()
        println("FAILED: ${e.message}")
        exitProcess(-1)
    }
}


fun testExistCharacter() {
    println("TEST: search exist character should return pre-defined structure with data")
    val characters = searchStarWarCharacters("darth vader")
    assertEqual(characters.size, 1)
    assertEqual(characters.first().toString(), """
* Name: Darth Vader
* Species: Human (Average Lifespan: 120 years)
* Home World: Tatooine
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope
""".trimIndent())
    println("PASS")
}

fun testNoneExistCharacter() {
    println("TEST: search none exist character should return empty character")
    val characters = searchStarWarCharacters("never existed before")
    assertEqual(characters.size, 0)
    println("PASS")
}


fun testMultiPageSearch() {
    println("TEST: search general term with multiple page should return max configured items")
    val characters = searchStarWarCharacters("a")
    assertEqual(characters.size, 10)
    println("PASS")
}

fun testInvalidApiRequest() {
    println("TEST: request with invalid api parameter should throw exception")
    var excption: Any? = null
    try {
        StarWarsApiClient().executeGetRequest<StarWarFilm>("${StarWarsApiClient().baseUrl}/path-not-exist")
    } catch (e: Exception) {
        excption = e
    }
    if (excption == null) {
        throw Exception("expected exception didn't happen")
    }
    println("PASS")
}


fun assertEqual(actual: Any, expected: Any) {
    if (actual != expected) {
        throw Exception("actual \n$actual\n!= expected\n$expected")
    }
}