package security.hashing

import model.security.SaltedHash

interface HashingService {

    fun generateSaltedHash(
        value: String,
        saltLength: Int = 32
    ): SaltedHash

    fun verify(
        value: String,
        saltedHash: SaltedHash
    ): Boolean
}