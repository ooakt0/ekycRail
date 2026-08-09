# ekycRail

Digital identity middleware platform designed specifically for the banking and financial sector in Nepal.

## Architecture baseline

eKYC Rail is a stateless B2B identity verification rail that connects BFIs in Nepal with upstream identity and compliance providers over OAuth 2.0 / OIDC.

### Mandatory platform rules

1. **Zero-PII storage**
   - Never persist identity fields such as NIN, full name, phone number, citizenship details, document numbers, or raw upstream responses.
   - Do not write user identity data to databases, queues, durable caches, object storage, or application logs.
   - Keep identity payloads only in request-scoped memory and clear references immediately after the response is generated.
2. **Strict log sanitization**
   - Only log transaction metadata: `transactionId`, `bankId`, `timestamp`, `status`, and `latencyMs`.
   - Strip or hash sensitive headers, request parameters, and payload fields before they reach structured logging.
   - Never log access tokens, authorization codes, cookies, upstream payloads, or identity attributes.
3. **Resilient upstream integrations**
   - All government and AML adapters must use explicit timeouts, bounded retries, and circuit breakers.
   - Upstream failures must be isolated so one degraded provider does not cascade into platform-wide outages.
4. **Cryptographic integrity**
   - Standardize successful identity responses into OIDC-compatible JWTs.
   - Sign JWTs with asymmetric keys using `RS256` or `ES256`.
   - Retrieve signing keys from KMS or an equivalent managed key service; never hardcode private keys or secrets.
5. **Type-safe validation**
   - Validate every inbound request, outbound response, and upstream adapter contract with strict schemas.
   - Use Zod in TypeScript services or Jakarta/Jackson validation in Java services.

## Implementation standards

- Prefer small, modular, testable components and immutable transformations.
- Use environment variables for every environment-specific value, including upstream base URLs, KMS identifiers, timeouts, retry limits, and issuer/audience settings.
- Mock external network adapters in unit tests.
- Add focused unit tests for each business flow when application code is introduced.

## Security and privacy checklist

Any implementation added to this repository must satisfy all of the following:

- [ ] No user PII is persisted outside transient request memory.
- [ ] Logs contain only sanitized transaction metadata.
- [ ] Upstream integrations enforce timeouts, retries, and circuit breaking.
- [ ] JWT output is OIDC-compatible and signed with `RS256` or `ES256` keys from KMS.
- [ ] Endpoint and adapter contracts are schema-validated.
- [ ] No secrets or environment-specific values are hardcoded.
