# Unicorn Utterances CMS

This provides a managed post editing/review flow for the [Unicorn Utterances](https://unicorn-utterances.com) website.

The aim is to enable a post draft, review, and revision workflow without needing to clone the repo or wait for build previews.

We chose to build our own solution instead of integrating with existing options because:

- We want to published posts to be stored in Git rather than a database

- UU's markdown integrations create additional complexity for editors and real-time previews

- Existing options are not flexible enough to provide an intuitive experience while supporting our use case

Here's a rough flow chart of the planned functionality:

```mermaid
flowchart TD
    classDef action stroke-width:4px
    classDef dashed stroke-dasharray:5 5

    CreatePost[User creates a draft post]:::action --> DB[(Database)]
    DB --> EditPost[User edits their post]:::action
    EditPost --> WriteDB(Write to Database)
    WriteDB --> DB
    WriteDB --> TaskQueue(Task Queue):::dashed

    TaskQueue(Task Queue) --> SyncFork(Sync updates with the GitHub fork/branch)
    SyncFork --> HasFork{Does the user\nhave a fork yet?}
    HasFork -->|No| CreateFork(Fork the unicorn-utterances repo\nwith a branch matching the post slug) --> PushFork
    HasFork -->|Yes| PushFork(Merge changes and push)

    ForkUpdated[User pushes manual changes to their fork]:::action -->|GitHub sends a webhook event| TaskQueue

    LeftComments[Someone leaves a comment on a PR]:::action -->|GitHub sends a webhook event| TaskQueue1(Task Queue):::dashed
    TaskQueue1 --> SyncComments(Sync comments from any active pull requests) --> DB

    LintStatus[GitHub workflows pass/fail]:::action -->|GitHub sends a webhook event| TaskQueue2(Task Queue):::dashed
    TaskQueue2 --> SyncLint(Sync build status/feedback) --> DB

    DB --> PublishPost[User publishes their post]:::action
    PublishPost --> TaskQueue3(Task Queue):::dashed
    TaskQueue3 -->|Wait for any tasks to complete| CreatePR(Create a pull request)
```

## Getting Started

Prerequisites:
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Java 21](https://jdk.java.net/21/) (can also be installed via IntelliJ: File -> Project Structure -> Project -> SDK)
- [Node 20](https://nodejs.org/en/download)
- pnpm
- [Docker](https://www.docker.com/get-started/)

To start the postgres database:
```sh
cp .env.example .env
docker compose up --wait
```

To run the API server:
```sh
cd api
./gradlew run # (or use the Run button in IntelliJ)
```

To start the client:
```sh
cd client
pnpm install
pnpm run dev
```
