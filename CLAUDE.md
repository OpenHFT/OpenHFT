# Vibe Coding Branch

This is an experimental branch for autonomous development. Changes will be manually reviewed before creating PRs.

## Autonomy

- Proceed without asking for permission on routine operations
- Run tests freely (`mvn test`, `mvn verify`)
- Edit and create files as needed
- Make commits frequently with clear messages

## Constraints

- Do not push to remote — I will push after review
- Do not rebase or rewrite history
- Do not delete directories recursively without explicit confirmation
- Stay within the scope of the task given

## Code Standards

- Follow existing code style in the repository
- Run `mvn test` after significant changes to catch regressions early
- Prefer small, incremental commits over large monolithic changes

## When Uncertain

If a change seems risky or outside the stated task scope, describe what you'd like to do and wait for confirmation rather than proceeding.

## Commit Messages

Use conventional format:
```
type(scope): brief description

Longer explanation if needed.
```

Types: feat, fix, refactor, test, docs, chore
