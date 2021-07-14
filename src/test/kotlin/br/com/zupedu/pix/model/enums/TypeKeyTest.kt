package br.com.zupedu.pix.model.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TypeKeyTest {

    @Nested
    inner class CPF {

        @Test
        fun`should be validate with cpf is null or blank`() {
            with(TypeKey.CPF) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }

        @Test
        fun`should be validate with cpf is invalid`() {
            with(TypeKey.CPF) {
            assertFalse(validate("999.222-invalid-62"))
            }
        }

        @Test
        fun`should be validate with cpf valid`(){
            with(TypeKey.CPF) {
                assertTrue(validate("10284673030"))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun`should be validate with email is null or blank`() {
            with(TypeKey.EMAIL) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }

        @Test
        fun`should be validate with email valid`(){
            with(TypeKey.EMAIL) {
                assertTrue(validate("test@email.com"))
            }
        }
    }

    @Nested
    inner class NUMBERCELL {
        @Test
        fun`should be validate with number cell is null or blank`() {
            with(TypeKey.NUMBER_CELL) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }

        @Test
        fun`should be validate with number cell valid`(){
            with(TypeKey.NUMBER_CELL) {
                assertTrue(validate("+5511912342345"))
            }
        }
    }

    @Nested
    inner class RANDOMKEY {
        @Test
        fun`should be validate with random key is null or blank`() {
            with(TypeKey.RANDOM_KEY) {
                assertTrue(validate(null))
                assertTrue(validate(""))
            }
        }

        @Test
        fun`should be validate as false with random key filled in`() {
            with(TypeKey.RANDOM_KEY) {
                assertFalse(validate("some data"))
            }
        }
    }
}

