package io.oath.csrf

import io.oath.csrf.config.CsrfConfig
import io.oath.csrf.model.CsrfToken
import io.oath.csrf.testkit.{AnyWordSpecBase, PropertyBasedTesting}

class CsrfManagerSpec extends AnyWordSpecBase with PropertyBasedTesting {

  "CsrfManager" when {

    "issueCSRF" should {
      "generate a valid token when secret is provided" in forAll { csrfConfig: CsrfConfig =>
        val csrfManager  = new CsrfManager(csrfConfig)
        val csrfToken    = csrfManager.issueCSRF()
        val expectedSize = 2

        csrfToken should not be empty
        csrfToken.value.token.value.split("-", 2).length shouldBe expectedSize
        csrfToken.value.token.value.split("-").head.toLong shouldBe a[Long]
      }
    }

    "verifyCSRF" should {
      "verify a valid token with the same secret" in forAll { csrfConfig: CsrfConfig =>
        val csrfManager = new CsrfManager(csrfConfig)
        val csrfToken   = csrfManager.issueCSRF()

        csrfManager.verifyCSRF(csrfToken.value) shouldBe true
      }

      "failed to verify when token provided has invalid structure" in {
        (csrfConfig: CsrfConfig, csrfToken: CsrfToken) =>
          val csrfManager = new CsrfManager(csrfConfig)

          csrfManager.verifyCSRF(csrfToken) shouldBe false
      }

      "failed to verify when token provided has valid structure but wrong key" in {
        (csrfConfig1: CsrfConfig, csrfConfig2: CsrfConfig) =>
          whenever(csrfConfig1.secret != csrfConfig2.secret) {
            val csrfManager1 = new CsrfManager(csrfConfig1)
            val csrfManager2 = new CsrfManager(csrfConfig2)

            csrfManager1.verifyCSRF(csrfManager2.issueCSRF().value) shouldBe false
          }
      }
    }
  }
}
