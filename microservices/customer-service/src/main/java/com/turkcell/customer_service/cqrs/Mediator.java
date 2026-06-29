package com.turkcell.customer_service.cqrs;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Central dispatcher that routes Commands and Queries to their registered handlers.
 * <p>
 * Handlers are discovered automatically from the Spring application context.
 * Generic type parameters are resolved via reflection to match the correct handler.
 * </p>
 */
@Component
public class Mediator implements ApplicationContextAware {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    /**
     * Dispatches a command to its registered {@link ICommandHandler}.
     *
     * @param command the command to dispatch
     * @param <R>     the result type
     * @return the result produced by the handler
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> R send(ICommand<R> command) {
        Map<String, ICommandHandler> handlers = ctx.getBeansOfType(ICommandHandler.class);
        for (ICommandHandler handler : handlers.values()) {
            if (canHandle(handler, ICommandHandler.class, command.getClass())) {
                return (R) handler.handle(command);
            }
        }
        throw new IllegalStateException("No ICommandHandler found for: " + command.getClass().getSimpleName());
    }

    /**
     * Dispatches a query to its registered {@link IQueryHandler}.
     *
     * @param query the query to dispatch
     * @param <R>   the result type
     * @return the result produced by the handler
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> R send(IQuery<R> query) {
        Map<String, IQueryHandler> handlers = ctx.getBeansOfType(IQueryHandler.class);
        for (IQueryHandler handler : handlers.values()) {
            if (canHandle(handler, IQueryHandler.class, query.getClass())) {
                return (R) handler.handle(query);
            }
        }
        throw new IllegalStateException("No IQueryHandler found for: " + query.getClass().getSimpleName());
    }

    /**
     * Checks whether a handler's first generic type argument matches the given message class.
     */
    private boolean canHandle(Object handler, Class<?> handlerInterface, Class<?> messageClass) {
        for (Type iface : handler.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType().equals(handlerInterface)) {
                Type firstArg = pt.getActualTypeArguments()[0];
                if (firstArg.equals(messageClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
