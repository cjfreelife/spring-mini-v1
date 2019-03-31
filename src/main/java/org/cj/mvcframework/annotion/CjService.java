package org.cj.mvcframework.annotion;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CjService {
    String value() default "";
}
