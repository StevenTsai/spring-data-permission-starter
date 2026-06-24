# Contributing to spring-data-permission-starter

Thank you for your interest in contributing! This document provides guidelines for contributing to this project.

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.8+
- Git

### Setup

```bash
git clone https://github.com/StevenTsai/spring-data-permission-starter.git
cd spring-data-permission-starter
mvn clean install
```

### Running the Sample

```bash
cd spring-data-permission-samples/sample-basic
mvn spring-boot:run
```

Then test with curl:

```bash
# Admin user (ALL scope)
curl -H "X-User-Id: 1" http://localhost:8080/api/orders

# Staff user (SELF scope)
curl -H "X-User-Id: 4" http://localhost:8080/api/orders
```

## Development

### Project Structure

```
spring-data-permission-core/       # Core abstractions (no MyBatis dependency)
spring-data-permission-mybatis/    # MyBatis integration
spring-data-permission-spring-boot-starter/  # Auto-configuration
spring-data-permission-samples/    # Sample applications
```

### Code Style

- Follow standard Java conventions
- Use Lombok for boilerplate reduction
- Add JavaDoc to all public interfaces and classes
- Keep methods short and focused

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl spring-data-permission-core
mvn test -pl spring-data-permission-mybatis
```

## How to Contribute

### Reporting Bugs

Open a GitHub issue with:

- A clear, descriptive title
- Steps to reproduce
- Expected vs actual behavior
- Your environment (JDK version, Spring Boot version, MyBatis version)

### Suggesting Features

Open a GitHub issue with the `enhancement` label. Describe:

- The problem you're trying to solve
- Your proposed solution
- Any alternatives you've considered

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Add or update tests as needed
5. Ensure all tests pass: `mvn test`
6. Commit with a clear message
7. Push to your fork and open a PR

### PR Guidelines

- Keep PRs focused — one feature or fix per PR
- Include tests for new functionality
- Update documentation if needed
- Reference related issues (e.g. `Fixes #12`)

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
