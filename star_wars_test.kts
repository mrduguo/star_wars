#!/usr/bin/env kscript
@file:Include("star_wars.kts")
@file:DependsOn("org.slf4j:slf4j-simple:1.7.26")
@file:DependsOn("io.kotlintest:kotlintest-runner-junit5:3.3.2")
@file:DependsOn("io.kotlintest:kotlintest-runner-console:3.3.2")


import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.matchers.string.shouldHaveLineCount
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec

class StarWarsTest : FreeSpec({

    "happy paths" -{

        val starWars = StarWars()

        "show usage should return instructions" {
            starWars.showUsage() shouldBe  "Usage:\n  ./star_wars <search term>"
        }

        "exact character name search should return the pre-defined structure" {
            starWars.executeSearch("darth vader") shouldBe """
                        * Name: Darth Vader
                        * Species: Human (Average Lifespan: 120 years)
                        * Home World: Tatooine
                        * Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope
                        """.trimIndent()
        }

        "none exist name search should return error message" {
            starWars.executeSearch("never existed before") shouldBe "no character found for search term <never existed before>"
        }

        "multi page search result should return the pre-defined structure" {
            starWars.executeSearch("a") shouldHaveLineCount 48
        }

    }

    "sad paths" - {

        "failed dependent API response should throw exception" {
            val exception = shouldThrow<Exception> {
                val starWarsApiClient=StarWarsApiClient()
                starWarsApiClient.executeGetRequest<StarWarFilm>("${starWarsApiClient.baseUrl}/path-not-exist")
            }
            exception.message shouldStartWith "Failed to execute request with response: <-- 404"
        }

        "invalid host name base url should throw exception" {
            val exception = shouldThrow<Exception> {
                withEnvironment("STAR_WARS_API_BASE_URL", "http://host.not.exist") {
                    StarWarsApiClient().searchCharactersByTerm("/path-not-exist")
                }
            }
            exception.message shouldStartWith "Failed to execute request with response: <-- -1"
        }

        "invalid max value throw exception" {
            val exception = shouldThrow<NumberFormatException> {
                withEnvironment("STAR_WARS_API_SEARCH_MAX_RESULT", "NOT_A_NUMBER_VALUE") {
                    StarWarsApiClient()
                }
            }
            exception.message shouldBe "For input string: \"NOT_A_NUMBER_VALUE\""
        }

    }

})

io.kotlintest.runner.console.main(arrayOf())
