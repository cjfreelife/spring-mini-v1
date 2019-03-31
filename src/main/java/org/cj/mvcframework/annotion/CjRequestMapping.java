package org.cj.mvcframework.annotion;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CjRequestMapping {
    String value() default "";
}
