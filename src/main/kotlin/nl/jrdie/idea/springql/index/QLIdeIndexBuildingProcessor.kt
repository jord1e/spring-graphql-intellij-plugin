package nl.jrdie.idea.springql.index

@FunctionalInterface
interface QLIdeIndexBuildingProcessor<T, B : QLIdeIndex.Builder<B>> {

    fun process(t: T, indexBuilder: B): B
}
