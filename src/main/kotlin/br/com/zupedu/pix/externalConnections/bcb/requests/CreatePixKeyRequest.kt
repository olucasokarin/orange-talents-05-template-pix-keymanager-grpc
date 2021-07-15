package br.com.zupedu.pix.externalConnections.bcb.requests

import br.com.zupedu.pix.model.KeyPix
import br.com.zupedu.pix.model.enums.TypeAccount
import br.com.zupedu.pix.model.enums.TypeKey
import java.lang.IllegalArgumentException
import javax.validation.constraints.Size

data class CreatePixKeyRequest(
    val keyType: KeyTypeBCB,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
) {
    companion object {
        fun receiveEntityToPixRequest(keyPix: KeyPix) =
            CreatePixKeyRequest(
                keyType = KeyTypeBCB.by(keyPix.typeKey),
                key = keyPix.valueKey,
                bankAccount = BankAccountRequest(
                    participant = keyPix.institution.ispb,
                    branch = keyPix.branch,
                    accountNumber = keyPix.accountNumber,
                    accountType = AccountType.by(keyPix.typeAccount)
                ),
                owner = OwnerRequest(
                    type = OwnerType.NATURAL_PERSON,
                    name = keyPix.owner.nome,
                    taxIdNumber = keyPix.owner.cpf
                )
            )
    }
}

data class OwnerRequest(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)

data class BankAccountRequest(
    val participant: String, //60701190
    @field:Size(min = 4, max = 4)
    val branch: String, //agencia
    @field:Size(min = 6, max = 6)
    val accountNumber: String,
    val accountType: AccountType
)

enum class OwnerType {
    NATURAL_PERSON,
    LEGAL_PERSON;
}

enum class AccountType {
    CACC,
    SVGS;

    companion object {
        fun by(domain: TypeAccount) =
            when(domain) {
                TypeAccount.CHECKING_ACCOUNT -> CACC
                TypeAccount.SAVINGS_ACCOUNT -> SVGS
            }
    }
}

enum class KeyTypeBCB(val domainKey: TypeKey?) {
    CPF(TypeKey.CPF),
    CNPJ(null),
    PHONE(TypeKey.NUMBER_CELL),
    EMAIL(TypeKey.EMAIL),
    RANDOM(TypeKey.RANDOM_KEY);

    companion object {
        private val mapping = KeyTypeBCB.values().associateBy(KeyTypeBCB::domainKey)

        fun by(domainKey: TypeKey?) =
            mapping[domainKey] ?:
            throw IllegalArgumentException("KeyTypeBCB invalid or not found")
    }
}
