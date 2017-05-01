# wikiracer

## Installation

### Java

You need to install Java. Feel free to download the latest JDK from [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html), or use [brew cask](https://caskroom.github.io/).

## Usage

Run the uberjar:

    $ java -jar target/wikiracer-standalone.jar

## Approach

I decided to use the [WikiMedia API](https://www.mediawiki.org/wiki/API:Query) to traverse links to other articles. I went through several approaches leveraging the API, with varying success.


### Brute Force BFS
In this approach I used a simple BFS approach with no parallelism.
I used a set to represent the pages that had already been visited. Whenever a request came back with neighbor links to explore, already visited links were filtered. The remaining links were then added to a queue to query on the next cycle.

The program would terminate immediately if the destination link was found among a set of neighbors. The program woudld also terminate after a max depth is reached, if not link can be found by then.

### Attempt Two: Parallel BFS
In this approach, I spun up N threads to make the requests using [claypoole](https://github.com/TheClimateCorporation/claypoole).
Whenever a thread would return a result, its results were added to a lazy sequence to compute for the next BFS cycle.

This approach would have been ideal if it worked as the threadpool minimized the effect of the Network I/O. Unfortunately, the WikiMedia API rate limits the requests and start rejecting them at even a modest thread count.

I also tried using a [generator](https://www.mediawiki.org/wiki/API:Query#Generators) that could query two levels of neighbors at a time, but this call was very expensive. It also became harder to keep track of the current path when looking two levels ahead, and the rate limits persisted. So I bailed on this approach.

### Attempt Three (final): Laziness

I decided to only partially compute each of the BFS computations for each level, in the chance the some sources->destinations had many paths.

This seemed to work really well for those cases.

```clojure

source => "2017 London Marathon"

destination => "Washington, D.C."

max-depth => 6
2017 London Marathon -> 2010 London Marathon -> 2010 Berlin Marathon -> 2005 Berlin Marathon -> Andrew Letherby -> Sport of athletics -> Washington, D.C.
Time (ms): 1233
:done
```


```clojure
source => "Grand National Trial"

destination => "Mike Tyson"

max-depth => 6

Grand National Trial -> Adrian Maguire -> 2011 Grand National -> 1836 Grand Liverpool Steeplechase -> England -> Boxing -> Mike Tyson
Time (ms): 5092
:done
```

For cases where there were fews paths between two nodes, computation could take tens of minutes for a depth of 6.

## Future Improvements

### Check that source and destination node exist
I did not have time to do this.

### Better Caching
I used [atoms](https://clojure.org/reference/atoms) to represent my state, with the visited set being a set of strings.
Leveraging a database like Redis would help the algorithm scale to larger depths.

### Download Wikipedia
One large upfront cost would allow for many fast computations to run on a local database.

### A*
Use heuristics to pick which nodes to visit next, rather than guessing blindly.

### Randomize lazy evaluating
Currently the lazy evaluation takes advantages of paths where the links are earlier on in the list. Using randomness would better take advantage of highly connected nodes.
