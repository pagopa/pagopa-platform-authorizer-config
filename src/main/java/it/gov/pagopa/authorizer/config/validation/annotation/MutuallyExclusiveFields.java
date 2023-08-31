package it.gov.pagopa.authorizer.config.validation.annotation;

import it.gov.pagopa.authorizer.config.validation.validator.MutuallyExclusiveFieldsValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MutuallyExclusiveFieldsValidator.class)
public @interface MutuallyExclusiveFields {

  String[] fields();

  boolean canBeBothNull() default true;

  String message() default "{validation.mutually-exclusive.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
