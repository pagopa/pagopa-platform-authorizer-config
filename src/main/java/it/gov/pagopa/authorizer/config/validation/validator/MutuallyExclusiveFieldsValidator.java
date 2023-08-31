package it.gov.pagopa.authorizer.config.validation.validator;

import it.gov.pagopa.authorizer.config.validation.annotation.MutuallyExclusiveFields;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class MutuallyExclusiveFieldsValidator implements ConstraintValidator<MutuallyExclusiveFields, Object> {

  private Set<String> fields = new HashSet<>();

  private boolean canBeBothNull = false;

  @Override
  public void initialize(MutuallyExclusiveFields constraintAnnotation) {
    this.fields.addAll(Set.of(constraintAnnotation.fields()));
    this.canBeBothNull = constraintAnnotation.canBeBothNull();
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @SuppressWarnings("java:S3011")
  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    Class<?> clazz = value.getClass();
    int numberOfNotNullElements = 0;
    try {
      for (String field : fields) {
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        numberOfNotNullElements += objectField.get(value) != null ? 1 : 0;
      }
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalArgumentException("Defined invalid field in the MutuallyExclusive annotation.", e);
    }
    // check if mutual exclusivity is broken and if so define a custom message
    boolean breakMutualExclusivity = numberOfNotNullElements > 1;
    boolean breakMutualExclusivityForNull = !canBeBothNull && numberOfNotNullElements == 0;
    if (breakMutualExclusivity) {
      ((ConstraintValidatorContextImpl) context).addMessageParameter("validation.mutually-exclusive.message", String.format("Two or more value for the tags %s are null, but only one of them must have values.", fields));
    }
    if (breakMutualExclusivityForNull) {
      ((ConstraintValidatorContextImpl) context).addMessageParameter("validation.mutually-exclusive.message", String.format("All value for the tags %s are not null, but only one of them must have values.", fields));
    }
    // return the checks in AND
    return !breakMutualExclusivity && !breakMutualExclusivityForNull;
  }

}
