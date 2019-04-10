#!/usr/bin/env kscript

@file:DependsOn("com.github.kittinunf.fuel:fuel-gson:2.0.1")

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.gson.responseObject
import java.net.URLEncoder


/*
     _______.___________.    ___      .______         ____    __    ____  ___      .______          _______.
    /       |           |   /   \     |   _  \        \   \  /  \  /   / /   \     |   _  \        /       |
   |   (----`---|  |----`  /  ^  \    |  |_)  |        \   \/    \/   / /  ^  \    |  |_)  |      |   (----`
    \   \       |  |      /  /_\  \   |      /          \            / /  /_\  \   |      /        \   \
.----)   |      |  |     /  _____  \  |  |\  \----.      \    /\    / /  _____  \  |  |\  \----.----)   |
|_______/       |__|    /__/     \__\ | _| `._____|       \__/  \__/ /__/     \__\ | _| `._____|_______/

 */


if (isNotTest()) {
    val searchTerm = args.joinToString(" ")
    val starWars = StarWars()

    println(
            if (searchTerm.isNotEmpty()) {
                starWars.executeSearch(searchTerm)
            } else {
                starWars.showUsage()
            }
    )
}

fun isNotTest() = !(System.getenv("KSCRIPT_FILE") ?: System.getenv("_")).contains("_test")


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

    fun searchCharactersByTerm(searchTerm: String): List<StarWarCharacter> {
        val resultList = mutableListOf<StarWarCharacter>()
        var searchUrl = "$baseUrl/people/?search=${URLEncoder.encode(searchTerm, "UTF-8")}"
        loop@ while (true) {
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


class StarWars {
    val starWarsApiClient = StarWarsApiClient()

    fun showUsage() = "Usage:\n  ./star_wars <search term>"

    fun executeSearch(searchTerm: String): String {
        val characters = searchStarWarCharacters(searchTerm)
        if (characters.isNotEmpty()) {
            return characters.joinToString("\n========================================================\n")
        } else {
            return "no character found for search term <$searchTerm>"
        }
    }

    private fun searchStarWarCharacters(searchTerm: String): List<StarWarCharacter> {
        val characters = starWarsApiClient.searchCharactersByTerm(searchTerm)
        characters.forEach { enrichCharacter(it) }
        return characters
    }

    private fun enrichCharacter(character: StarWarCharacter) {
        character.homeworld = starWarsApiClient.executeGetRequest<StarWarHomeWorld>(character.homeworld).name
        character.species = character.species.map { species ->
            starWarsApiClient.executeGetRequest<StarWarSpecies>(species).toString()
        }
        character.films = character.films.map { film ->
            starWarsApiClient.executeGetRequest<StarWarFilm>(film).title
        }
    }

}