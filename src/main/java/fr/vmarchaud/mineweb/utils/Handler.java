package fr.vmarchaud.mineweb.utils;

/**
 * A generic event handler
 * <p>
 * 
 * This interface is used heavily throughout vert.x as a handler for all types
 * of asynchronous occurrences.
 * <p>
 * 
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://alecgorge.com">Alec Gorge</a>
 */
public interface Handler<R, E> {

	/**
	 * Something has happened, so handle it.
	 */
	R handle(E event);
}
