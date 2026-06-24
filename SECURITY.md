# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, please send an email to the maintainer with:

1. A description of the vulnerability
2. Steps to reproduce the issue
3. The potential impact
4. Any suggested fix (if available)

### What to Expect

- **Acknowledgment**: We will acknowledge receipt of your report within 48 hours.
- **Assessment**: We will assess the vulnerability and determine its severity.
- **Fix**: We will work on a fix and aim to release a patch as soon as possible.
- **Disclosure**: We will coordinate with you on the disclosure timeline.

## Security Considerations

This library controls **row-level data access permissions**. Misconfiguration or bypass could lead to unauthorized data exposure. Please ensure:

1. **Always test permission rules** in a staging environment before deploying to production.
2. **Validate `PermissionResolver` implementations** thoroughly — they are the single source of truth for access control.
3. **Never trust client-supplied org IDs or user IDs** without server-side verification.
4. **Use the `checkAccess()` method** for single-record access validation, not just list filtering.
5. **Keep dependencies up to date** to pick up security patches from Spring Boot and MyBatis.
