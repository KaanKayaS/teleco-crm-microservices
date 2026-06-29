package com.turkcell.customer_service.cqrs;

/**
 * Contract for all Command handlers in the CQRS pipeline.
 *
 * @param <C> the command type
 * @param <R> the result type
 */
public interface ICommandHandler<C extends ICommand<R>, R> {
    R handle(C command);
}
