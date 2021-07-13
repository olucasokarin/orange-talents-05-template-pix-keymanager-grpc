package br.com.zupedu.pix

import br.com.zupedu.KeyPixRequest
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidKeyPixValidator::class])
annotation class ValidKeyPix(
    val message: String = "Key Pix invalid '\${validatedValue.typeKey}'",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidKeyPixValidator : ConstraintValidator<ValidKeyPix, KeyPixRequest> {
    override fun isValid(value: KeyPixRequest?, context: ConstraintValidatorContext?): Boolean {
        if(value?.typeKey == null) return true

        val valid = value.typeKey.validate(value.valueKey)

        if(!valid) {
            context?.disableDefaultConstraintViolation()
            context
                ?.buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                ?.addPropertyNode("key")?.addConstraintViolation()
        }
        return valid
    }

}
