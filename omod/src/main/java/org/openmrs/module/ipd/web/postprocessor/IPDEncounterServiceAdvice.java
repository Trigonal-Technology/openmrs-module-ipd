package org.openmrs.module.ipd.web.postprocessor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Spring AOP method interceptor that replaces the emrapi
 * {@code EncounterTransactionHandler} hook.
 *
 * <p>
 * This interceptor wraps
 * {@link org.openmrs.api.EncounterService#saveEncounter(Encounter)}
 * and calls {@link IPDTransactionHandler#afterSave(Encounter)} after a
 * successful save.
 *
 * <p>
 * To activate this advice, register it in
 * {@code webModuleApplicationContext.xml} as an advisor
 * on the {@code encounterService} bean:
 * 
 * <pre>{@code
 * <bean id="ipdEncounterServiceAdvice"
 *       class=
"org.openmrs.module.ipd.web.postprocessor.IPDEncounterServiceAdvice"/>
 * <advisor id="ipdEncounterAdvisor"
 *          advice-ref="ipdEncounterServiceAdvice"
 *          pointcut=
"execution(* org.openmrs.api.EncounterService.saveEncounter(..))"/>
 * }</pre>
 */
@Component
public class IPDEncounterServiceAdvice implements MethodInterceptor {

    private final IPDTransactionHandler ipdTransactionHandler;

    @Autowired
    public IPDEncounterServiceAdvice(IPDTransactionHandler ipdTransactionHandler) {
        this.ipdTransactionHandler = ipdTransactionHandler;
    }

    /**
     * Intercepts {@code EncounterService.saveEncounter} and triggers IPD post-save
     * logic.
     *
     * @param invocation the method invocation
     * @return the return value of the intercepted method (the saved Encounter)
     * @throws Throwable if the underlying method throws
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof Encounter) {
            ipdTransactionHandler.afterSave((Encounter) result);
        }
        return result;
    }
}
