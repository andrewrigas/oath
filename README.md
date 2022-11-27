[![Build status](https://img.shields.io/github/workflow/status/andrewrigas/oath/Continuous%20Integration.svg)](https://github.com/andrewrigas/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/circe/circe/master.svg)](https://codecov.io/github/circe/circe)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewrigas/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.andrewrigas/jwt-core_2.13/0.0.6)

## OATH

__OATH__ provides a set of tools for authentication/authorization of users.

### Description



## Libraries

* __JWT__ - The core library that provides an Issuer/Verifier/Manager to manipulate JWT's
  * __Circe__ - an implicit convertion for converting Encoder/Decoder from Circe to ClaimEncoder/ClaimDecoder

### JWT

```scala
libraryDependencies += "io.github.andrewrigas" %% "jwt-core" % "0.0.4"
// Circe integration
libraryDependencies += "io.github.andrewrigas" %% "jwt-circe" % "0.0.4"
```

Config file for `JWT Manager`
```hocon
xxxx-xxxx {
  algorithm {
    name = "HS256"
    secret-key = "secret"
  }
}

token {
// For asymmetric algorithm
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem"
    public-key-pem-path = "src/test/secrets/rsa-public.pem"
  }
// For symmetric algorithm  
//  algorithm {
//    name = "HS256"
//    secret-key = "secret"
//  }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = ["aud1", "aud2"]
      include-issued-at-claim = true
      include-jwt-id-claim = false
      expires-at-offset = 1 day
      not-before-offset = 1 minute
    }
  }
  verifier {
    provided-with {
      issuer-claim = ${token.issuer.registered.issuer-claim}
      subject-claim = ${token.issuer.registered.subject-claim}
      audience-claims = ${token.issuer.registered.audience-claims}
    }
    leeway-window {
      leeway = 1 min
      issued-at = 4 min
      expires-at = 3 min
      not-before = 2 min
    }
  }
}
```

Config file for `JWT Issuer`

```hocon
token {
// For asymmetric algorithm
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem"
  }
// For symmetric algorithm  
//  algorithm {
//    name = "HS256"
//    secret-key = "secret"
//  }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = ["aud1", "aud2"]
      include-issued-at-claim = true
      include-jwt-id-claim = false
      expires-at-offset = 1 day
      not-before-offset = 1 minute
    }
  }
}
```

Config file for `JWT Verifier`

```hocon
token {
// For asymmetric algorithm  
  algorithm {
    name = "RS256"
    public-key-pem-path = "src/test/secrets/rsa-public.pem"
  }
// For symmetric algorithm  
//  algorithm {
//    name = "HS256"
//    secret-key = "secret"
//  }
  verifier {
    provided-with {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = ["aud1", "aud2"]
    }
    leeway-window {
      leeway = 1 minute
      issued-at = 4 minutes
      expires-at = 3 minutes
      not-before = 2 minutes
    }
  }
}
```
