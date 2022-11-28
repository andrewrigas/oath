[![Build status](https://img.shields.io/github/workflow/status/andrewrigas/oath/Continuous%20Integration.svg)](https://github.com/andrewrigas/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/andrewrigas/oath/master.svg)](https://codecov.io/github/andrewrigas/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewrigas/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.andrewrigas/jwt-core_2.13/0.0.6)

# JWT (JSON Web Token)

The JWT layer depends on [oath0/java-jwt](https://github.com/auth0/java-jwt) library. Is inspired
by [akka-http-session](https://github.com/softwaremill/akka-http-session)
& [scala-jwt](https://github.com/jwt-scala/jwt-scala)
if you have already used those libraries you would probably find your self familiar with this API.

---

## SBT Dependencies

```scala
libraryDependencies += "io.github.andrewrigas" %% "jwt-core" % "0.0.0"
```

### Json Converters

```scala
libraryDependencies += "io.github.andrewrigas" %% "jwt-circe" % "0.0.0"

libraryDependencies += "io.github.andrewrigas" %% "jwt-jsoniter-scala" % "0.0.0"
```

---

## Introduction

### Supported Algorithms

| JWS | Algorithm | Description |
| :-------------: | :-------------: | :----- |
| HS256 | HMAC256 | HMAC with SHA-256 |
| HS384 | HMAC384 | HMAC with SHA-384 |
| HS512 | HMAC512 | HMAC with SHA-512 |
| RS256 | RSA256 | RSASSA-PKCS1-v1_5 with SHA-256 |
| RS384 | RSA384 | RSASSA-PKCS1-v1_5 with SHA-384 |
| RS512 | RSA512 | RSASSA-PKCS1-v1_5 with SHA-512 |
| ES256 | ECDSA256 | ECDSA with curve P-256 and SHA-256 |
| ES384 | ECDSA384 | ECDSA with curve P-384 and SHA-384 |
| ES512 | ECDSA512 | ECDSA with curve P-521 and SHA-512 |

### Model overview

All registered claims documented in [RFC-7519](https://www.rfc-editor.org/rfc/rfc7519.html) are provided with optional
values, therefore the library doesn't enforce you to use them.

```scala
final case class RegisteredClaims(iss: Option[NonEmptyString], sub: Option[NonEmptyString], aud: Seq[NonEmptyString], exp: Option[Instant], nbf: Option[Instant], iat: Option[Instant], jti: Option[NonEmptyString])
```

Claims is more than Registered Claims though. Therefore, if the business requirements requires extra claims to be able
to authenticate & authorize the client,
the library provides an ADT to describe each use case and the location for additional claims.

```scala
sealed trait JwtClaims

object JwtClaims {

  final case class Claims(registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsH[+H](header: H, registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsP[+P](payload: P, registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsHP[+H, +P](header: H, payload: P, registered: RegisteredClaims) extends JwtClaims
}
```

The JWT (JSON Web Token) is described as a whole with the `claims` & `token` in
the below data structure. The `token` is in this form `base64(header).base64(payload).signature`.

```scala
final case class Jwt[+C <: JwtClaims](claims: C, token: NonEmptyString)
```

### Issuer & Verifier & Manager

#### Issuer Overview

```scala
import io.circe.generic.auto._
import io.oath.syntax._
import io.oath.circe._

final case class Foo(name: String, age: Int)

val config = IssuerConfig.loadOrThrow("token") // HMAC256 with "secret" as secret
val issuer = new JwtIssuer(config)
val foo = Foo("foo", 10)

val maybeJwt: Either[IssueJwtError, Jwt[JwtClaims.ClaimsP[Foo]]] = issuer.issueJwt(foo.toClaimsP)

// Right(Jwt(ClaimsP(Foo(foo,10),RegisteredClaims(None,None,List(),None,None,None,None)),eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiYWdlIjoxMH0.oeU3zySKPA-fowGQkl0WPDwyBhXJUEtobSjGQsDBXcs))
```

#### Verifier Overview

```scala
import io.circe.generic.auto._
import eu.timepit.refined.auto._
import io.oath.syntax._
import io.oath.circe._

final case class Foo(name: String, age: Int)

val config = VerifierConfig.loadOrThrow("token") // HMAC256 with "secret" as secret
val verifier = new JwtVerifier(config)
val token: NonEmptyString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiYWdlIjoxMH0.oeU3zySKPA-fowGQkl0WPDwyBhXJUEtobSjGQsDBXcs"

val claims: Either[JwtVerifyError, JwtClaims.ClaimsP[Foo]] = verifier.verifyJwt[Foo](token.toTokenP)

// Right(ClaimsP(Foo(foo,10),RegisteredClaims(None,None,List(),None,None,None,None)))
```

#### Manager Overview

```scala
import io.circe.generic.auto._
import eu.timepit.refined.auto._
import io.oath.syntax._
import io.oath.circe._

final case class Foo(name: String, age: Int)

val config = ManagerConfig.loadOrThrow("token")
val manager = new JwtManager(config)
val foo = Foo("foo", 10)

val jwt: Jwt[JwtClaims.ClaimsP[Foo]] = manager.issueJwt(foo.toClaimsP) // ignore failures
val claims: JwtClaims.ClaimsP[Foo] = manager.verifyJwt[Foo](jwt.token.toTokenP) // ignore failures

```

### Ad-hoc Registered Claims

The library also provides ad-hoc claims manipulation with priority to the claims that have been provided by the code.

```hocon
token {
    algorithm { 
      name = "HMAC256"
      secret = "secret"
    }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
    }
  }
}
```

```scala
import io.circe.generic.auto._
import io.oath.jwt.model._
import io.oath.syntax._
import io.oath.circe._

final case class Foo(name: String, age: Int)

val config = IssuerConfig.loadOrThrow("token")
val issuer = new JwtIssuer(config)
val foo = Foo("foo", 10)
val adHocClaimsP = JwtClaims.ClaimsP(foo, RegisteredClaims.empty.copy(iss = Some("foo")))

val maybeJwt: Either[IssueJwtError, Jwt[JwtClaims.ClaimsP[Foo]]] = issuer.issueJwt(adHocClaimsP)

// Right(Jwt(ClaimsP(Foo(foo,10),RegisteredClaims(Some(foo),Some(subject),List(),None,None,None,None)),eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiaXNzIjoiaXNzdWVyIiwiYWdlIjoxMH0.Dlow6pYmJ-5STSuEzL3WYnjpCrGYMKzadIwlOK_WBBc))
```

### Configuration

#### Manager Config Sample

```hocon
token {
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem" // Required only for issuing
    public-key-pem-path = "src/test/secrets/rsa-public.pem" // Required only for verifying
  }
  //  algorithm { 
  //    name = "HMAC256"
  //    secret = "secret" When using HMAC single secret is required for both verifier and issuer
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
      leeway = 1 minutes
      issued-at = 4 minutes
      expires-at = 3 minutes
      not-before = 2 minutes
    }
  }
}
```

#### Issuer Config Sample

```hocon
token {
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem" // Required only for issuing
  }
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

#### Verifier Config

```hocon
token {
  algorithm {
    name = "RS256"
    public-key-pem-path = "src/test/secrets/rsa-public.pem" // Required only for verifying
  }
  verifier {
    provided-with {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = []
    }
    leeway-window {
      leeway = 1 minutes
      issued-at = 4 minutes
      expires-at = 3 minutes
      not-before = 2 minutes
    }
  }
}
```