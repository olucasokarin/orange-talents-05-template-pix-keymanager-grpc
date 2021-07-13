package br.com.zupedu.pix.model.enums

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TypeKey {
    CPF {
        override fun validate(key: String?): Boolean {
            if(key.isNullOrBlank() || !key.matches("^[0-9]{11}\$".toRegex()))
                return false

            return CPFValidator().run {
                initialize(null)
                isValid(key, null)
            }

        }
    },
    EMAIL {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false

            return EmailValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }
    },
    NUMBER_CELL {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false

            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    RANDOM_KEY {
        override fun validate(key: String?): Boolean =
            key.isNullOrBlank()

    };

    abstract fun validate(key: String?): Boolean
}
