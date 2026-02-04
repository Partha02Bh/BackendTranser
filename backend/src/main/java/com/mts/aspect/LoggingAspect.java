package com.mts.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*
 * Logs entry/exit for all service methods.
 * Useful for debugging and perf tracking.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.mts.service.*.*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint jp) throws Throwable {
        String method = jp.getSignature().toShortString();
        Object[] args = jp.getArgs();

        log.debug("--> {} args={}", method, Arrays.toString(args));

        long start = System.nanoTime();
        try {
            Object result = jp.proceed();
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            log.debug("<-- {} took {}ms result={}", method, elapsed, result);
            return result;

        } catch (Throwable t) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.debug("<-- {} failed after {}ms: {}", method, elapsed, t.getMessage());
            throw t;
        }
    }
}
