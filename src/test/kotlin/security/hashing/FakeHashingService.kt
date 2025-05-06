package security.hashing

import model.security.SaltedHash
import model.security.testSaltedHash

class FakeHashingService : HashingService {
    var shouldSucceed = true
    var saltedHashToReturn = testSaltedHash

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        return saltedHashToReturn
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return shouldSucceed
    }
}