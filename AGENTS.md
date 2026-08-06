# Project API workflow

Whenever an API is added or changed, complete all of the following in the same change:

1. Document the endpoint in Swagger UI/OpenAPI, including request and response schemas,
   HTTP status codes, authentication requirements, headers, and representative error examples.
2. Keep `src/main/resources/static/api_specification.html` synchronized with the implementation.
3. Add tests for the endpoint's successful and failure scenarios.
4. Extend the OpenAPI integration test so the generated `/v3/api-docs` contract is verified.
5. Run the full test suite before handing off the change.

# Code documentation workflow

This repository is maintained collaboratively, so code must explain its intent and contract to
reviewers who did not write it.

1. Add detailed documentation to every new or modified class, interface, enum, constructor, and
   method. For Java code, use Javadoc (`/** ... */`) by default instead of ordinary line comments.
2. Method Javadoc must explain what the method is responsible for, the important behavior or
   business rule it implements, and any non-obvious side effects. Document parameters, return
   values, and thrown exceptions with `@param`, `@return`, and `@throws` when applicable.
3. Class-level Javadoc must describe the component's role, its main collaborators, and where it
   fits in the application flow. DTO and domain-model documentation must also explain the meaning
   and constraints of important fields.
4. Add inline comments only where the implementation contains a non-obvious decision, workaround,
   or algorithm. Explain why the code exists rather than translating each statement into prose.
5. Keep documentation synchronized whenever behavior changes. Do not leave stale, misleading,
   placeholder, or mechanically generated comments.
6. Tests must document the scenario and expected behavior through descriptive test names and, when
   the setup or purpose is not immediately clear, Javadoc or a focused explanatory comment.
