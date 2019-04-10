# Star Wars Character Search Tool

This command line tool is to search your favourite Star Wars characters by name. 

## Usages


### Requirements
* `java` 8 or newer in your `PATH`.
* `MacOS` or `Linux`. The tool developed and tested in `macOS` and should work on any `linux` system.

### Download

[star_wars](https://raw.githubusercontent.com/mrduguo/star_wars/master/star_wars) (1.8MB)

After download, give execution permission by `chmod +x star_wars`.

### Command

~~~
./star_wars <search term>
~~~



### Sample search with single result
~~~
$ ./star_wars darth vader
* Name: Darth Vader
* Species: Human (Average Lifespan: 120 years)
* Home World: Tatooine
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope
~~~

### Sample search with two results
~~~
$ ./star_wars darth
* Name: Darth Vader
* Species: Human (Average Lifespan: 120 years)
* Home World: Tatooine
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope
========================================================
* Name: Darth Maul
* Species: Zabrak (Average Lifespan: unknown years)
* Home World: Dathomir
* Movies: The Phantom Menace
~~~

### Sample search with many results

You can control how many to return with environment variable `STAR_WARS_API_SEARCH_MAX_RESULT`, default to `10`:
~~~
$ STAR_WARS_API_SEARCH_MAX_RESULT=3 ./star_wars a
* Name: Luke Skywalker
* Species: Human (Average Lifespan: 120 years)
* Home World: Tatooine
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope, The Force Awakens
========================================================
* Name: Darth Vader
* Species: Human (Average Lifespan: 120 years)
* Home World: Tatooine
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope
========================================================
* Name: Leia Organa
* Species: Human (Average Lifespan: 120 years)
* Home World: Alderaan
* Movies: The Empire Strikes Back, Revenge of the Sith, Return of the Jedi, A New Hope, The Force Awakens
~~~


## Development

### File structures

* `README.md` - documentation
* `star_wars.kts` - main solution source code
* `star_wars_test.kts` - tests
* `star_wars` - binary distribution



### Requirement
You need have the `kscript` installed. Follow the official [installation](https://github.com/holgerbrandl/kscript#installation) guide to get installed.  

### Run the tool directly
~~~
$ ./star_wars.kts
Usage:
  ./star_wars <search term>
~~~

### Build the binary 
~~~
kscript --package star_wars.kts
~~~

Then you can find the binary been built at same folder as the script as `star_wars`

### Run test
~~~
$ ./src/star_wars_test.kts 
1)      happy paths
                show usage should return instructions
                exact character name search should return the pre-defined structure
                none exist name search should return error message
                multi page search result should return the pre-defined structure
        sad paths
                failed dependent API response should throw exception
                invalid host name base url should throw exception
                invalid max value throw exception

KotlinTest completed in 16 seconds, 16313 millis
Specs: completed 1, tests 9
Tests: passed 9, failed 0, ignored 0
~~~
