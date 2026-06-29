package com.turkcell.customer_service.cqrs;

/**
 * Contract for all Query handlers in the CQRS pipeline.
 *
 * @param <Q> the query type
 * @param <R> the result type
 */
public interface IQueryHandler<Q extends IQuery<R>, R> {
    R handle(Q query);
}
